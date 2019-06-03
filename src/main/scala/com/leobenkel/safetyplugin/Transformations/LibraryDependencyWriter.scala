package com.leobenkel.safetyplugin.Transformations

import com.leobenkel.safetyplugin.Config.SafetyConfiguration
import com.leobenkel.safetyplugin.Messages.CommonMessage.ResultMessages
import com.leobenkel.safetyplugin.Messages.{ErrorMessage, Errors}
import com.leobenkel.safetyplugin.Modules.{Dependency, NameOfModule}
import com.leobenkel.safetyplugin.Utils.LoggerExtended
import sbt._
import com.leobenkel.safetyplugin.Utils.ImplicitModuleToString._
import com.leobenkel.safetyplugin.Utils.EitherUtils._
import com.leobenkel.safetyplugin.Messages.CommonMessage._

private[safetyplugin] case class LibraryDependencyWriter(config: SafetyConfiguration) {

  private def getLibraryFiltered(
    log:       LoggerExtended,
    libraries: Seq[ModuleID]
  ): Seq[(ModuleID, Seq[NameOfModule])] = {
    libraries
      .map { m =>
        val knowledge = config.PackageKnownRiskDependencies.filterKeys(_ === m)
        val depToRemove = knowledge.values.flatten.toSeq.distinct
        log.debug(s"For ${m.prettyString}: ${knowledge.size}/${depToRemove.length} lib to remove")

        (m, depToRemove)
      }
  }

  private def removeBadDependencies(
    log:      LoggerExtended,
    module:   ModuleID,
    acc:      Seq[ModuleID],
    toRemove: NameOfModule
  ): (ModuleID, Seq[ModuleID]) = {
    val depsToReplace = config.NeedToBeReplaced.filter(_.key == toRemove.key)

    require(
      depsToReplace.isEmpty || depsToReplace.size == 1,
      s"Size was ${depsToReplace.size} with: " +
        s"\n${depsToReplace.map(_.toString).mkString("\n")}"
    )

    toRemove.exclusionRule match {
      case Right(er) if depsToReplace.nonEmpty =>
        val dependencyToInjectBack = depsToReplace
          .map(_.toModuleID).flattenEI
        require(dependencyToInjectBack.isEmpty || dependencyToInjectBack.size == 1)

        log.debug(s"> For ${module.prettyString} - Exclude ${toRemove.toString}")
        if (dependencyToInjectBack.nonEmpty) {
          log.debug(
            s">> replacing with: ${dependencyToInjectBack.head.prettyString}."
          )
        } else {
          log.debug(s">> removing entirely.")
        }

        (
          module.excludeAll(er),
          acc ++ dependencyToInjectBack.map(removeAllDependencies).flattenEI
        )
      case Left(ex) =>
        log.debug(s"Error for $toRemove -> Exclusion rule: $ex")
        (module, acc)
      case _ => (module, acc)
    }

  }

  private def rewriteLibraries(
    log:       LoggerExtended,
    libraries: Seq[ModuleID]
  ): Seq[ModuleID] = {
    log.separatorDebug("LibraryDependencyWriter.rewrite")
    log.debug(s"> Rewrite starting with ${libraries.size} libraries")

    val libraryFiltered = getLibraryFiltered(log, libraries)

    val (goodLibraries, libraryToEdit) = libraryFiltered.partition(_._2.isEmpty)

    log.debug(s"> Will edit ${libraryToEdit.size} libraries: ")
    libraryToEdit.map(_._1).prettyString(log, "rewriteLibraries-start")

    val output = libraryToEdit
      .map {
        case (m, thingsToRemove) =>
          thingsToRemove
            .foldLeft((m, Seq[ModuleID]())) {
              case ((module, acc), toRemove) => removeBadDependencies(log, module, acc, toRemove)
            }
      }
      .flatMap { case (m, listModule) => listModule :+ m }
      .distinct ++ goodLibraries.map(_._1)

    log.debug(s"> After replacement, we have ${output.size} libraries:")
    output.prettyString(log, "rewriteLibraries-end")
    output
  }

  /**
    * This method rewrite the libraries to make sure the risky dependencies are not fetch and
    * replaced.
    *
    * @param libraries the libraries fetch by the build, coming from [[sbt.Keys.allDependencies]]
    *
    * @return
    */
  def rewrite(
    log:         LoggerExtended,
    libraries:   Seq[ModuleID],
    inDebugMode: Boolean
  ): Either[Errors, Seq[ModuleID]] = {
    if (inDebugMode) {
      Right(libraries)
    } else {
      checkVersion(log, libraries) match {
        case Left(_) if log.isSoftError => Right(rewriteLibraries(log, libraries))
        case Left(er)                   => Left(er)
        case Right(_)                   => Right(rewriteLibraries(log, libraries))
      }
    }
  }

  private def removeAllDependencies(moduleID: ModuleID): Either[String, ModuleID] = {
    config.NeedToBeReplaced
      .foldLeft(Right(moduleID): Either[String, ModuleID]) {
        case (m, toRemove) =>
          m.flatMap { module =>
            // to not remove itself from itself
            if (toRemove =!= module) {
              toRemove.exclusionRule.right.map(module.excludeAll(_))
            } else {
              Right(module)
            }
          }
      }
  }

  /**
    * Remove all but the wanted library.
    *
    * @return
    */
  def filter(
    log:        LoggerExtended,
    libraries:  Seq[ModuleID],
    whatToKeep: Dependency,
    withScala:  Boolean
  ): Either[Errors, Seq[ModuleID]] = {
    val organizationScala = "org.scala-lang"
    val output = libraries
      .filter(m => (withScala && m.organization == organizationScala) || whatToKeep === m)
      .map(_.withConfigurations(None))

    log.debug(s"Found ${output.size} after filter libraries (${libraries.size}): ")
    output.prettyString(log, "filter")

    if (output.nonEmpty) {
      Right(output)
    } else {
      Left(s"Could not find ${whatToKeep.toString} among the input libraries".asErrors)
    }
  }

  /**
    * This will check that the libraries have the legal versions.
    *
    * @param libraries the libraries fetch by the build, coming from [[sbt.Keys.allDependencies]]
    * @param log       The current log
    */
  private def checkVersion(
    log:        LoggerExtended,
    libraries:  Seq[ModuleID],
    moreErrors: ErrorMessage = ErrorMessage.Empty
  ): ResultMessages = {
    log.separatorDebug("LibraryDependencyWriter.checkVersion")

    val allCorrectLibraries = config.CorrectVersions

    val librariesToCheck: Seq[ModuleID] = for {
      correctLibrary <- allCorrectLibraries
      library        <- libraries if correctLibrary === library
    } yield {
      library
    }

    log.debug(s"> Verifying version of libraries (${librariesToCheck.size}) :")
    librariesToCheck.prettyString(log, "checkVersion")

    (allCorrectLibraries
      .filter(_.version.isRight)
      .map(m => (m, m.version.right.get))
      .flatMap {
        case (correctModule, correctVersion) =>
          librariesToCheck
            .filter(m => (correctModule === m) && m.revision != correctVersion)
            .map { m =>
              val correctModuleToString = m
                .withRevision(correctVersion)
                .withName(correctModule.name)
                .prettyString

              s"${m.prettyString} should be $correctModuleToString"
            }
      }
      .toError("Wrong versions") ++ moreErrors)
      .resolve(
        s"> All ${librariesToCheck.size} libraries that we " +
          s"know have the right versions"
      )
  }

  def lastCheckUp(
    log:       LoggerExtended,
    allModule: Seq[Dependency]
  ): ResultMessages = {
    log.separatorDebug("LibraryDependencyWriter.lastCheckUp")
    val tooManyVersions = allModule.filter(_.tooManyVersions)
    val oneVersion = allModule.filter(_.toModuleID.isRight).map(_.toModuleID.right.get)

    val errors = tooManyVersions
      .map(m => s"More than one version of: ${m.toString} - [${m.versions.mkString(", ")}]")
      .toError("Found libraries with more than one version")
    checkVersion(log, oneVersion, errors)
  }

}
