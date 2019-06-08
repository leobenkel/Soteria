package com.leobenkel.safetyplugin.Transformations

import com.leobenkel.safetyplugin.Config.SafetyConfiguration
import com.leobenkel.safetyplugin.Messages.CommonMessage._
import com.leobenkel.safetyplugin.Messages.Errors
import com.leobenkel.safetyplugin.Modules.{Dependency, NameOfModule}
import com.leobenkel.safetyplugin.SafetyPluginKeys
import com.leobenkel.safetyplugin.Utils.EitherUtils._
import com.leobenkel.safetyplugin.Utils.ImplicitModuleToString._
import com.leobenkel.safetyplugin.Utils.LoggerExtended
import sbt._

private[Transformations] trait TaskAllDependencies extends CheckVersion {

  /**
    * This will rewrite the dependencies. And then rewrite the libraries with
    * exclusions and reinjection of the removed ones with the correct version. For more details,
    * look at [[rewriteLibAfterVersionCheck]] .
    * When debugging is enable, this will also remove all libraries but the tested one.
    */
  def allDependencies(): Def.Initialize[Task[Seq[sbt.ModuleID]]] = {
    Def.taskDyn {
      val log = SafetyPluginKeys.safetyGetLog.value
      val libraries = Keys.allDependencies.value
      val debugValue = SafetyPluginKeys.safetyDebugModule.value
      val debugWithScala = SafetyPluginKeys.safetyDebugWithScala.value
      val safetyConfig: SafetyConfiguration = SafetyPluginKeys.safetyConfig.value

      Def.task {
        log.separatorDebug("allDependencies")
        log.debug(s"> Start 'allDependencies' with ${libraries.size} libraries.")
        libraries.prettyString(log, "allDependencies")

        (if (debugValue.isDefined) {
           val (org, name) = debugValue.get
           val debugModule = Dependency(org, name)
           log.info(s"> Debug mode, filter with ${debugModule.toString}")
           filterLibraries(log, libraries, debugModule, debugWithScala)
         } else {
           rewriteLibAfterVersionCheck(log, safetyConfig, libraries, debugValue.isDefined)
         }) match {
          case Left(errors) =>
            if (!log.isSoftError) errors.consume(s => log.fail(s))
            Seq.empty
          case Right(rewroteLibraries) =>
            log.info(s"> 'allDependencies' have ${rewroteLibraries.size} libraries.")
            rewroteLibraries
        }
      }
    }
  }

  private val goodLibraries: Seq[String] = Seq(
    "com.github.pathikrit",
    "org.scala-lang"
  )

  /**
    * Remove all but the wanted library.
    *
    * @return
    */
  private def filterLibraries(
    log:        LoggerExtended,
    libraries:  Seq[ModuleID],
    whatToKeep: Dependency,
    withScala:  Boolean
  ): Either[Errors, Seq[ModuleID]] = {
    val output = libraries
      .filter(m => (withScala && goodLibraries.contains(m.organization)) || whatToKeep === m)
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
    * This method rewrite the libraries to make sure the risky dependencies are not fetch and
    * replaced.
    *
    * @param libraries the libraries fetch by the build, coming from [[sbt.Keys.allDependencies]]
    *
    * @return
    */
  private def rewriteLibAfterVersionCheck(
    log:         LoggerExtended,
    config:      SafetyConfiguration,
    libraries:   Seq[ModuleID],
    inDebugMode: Boolean
  ): Either[Errors, Seq[ModuleID]] = {
    if (inDebugMode) {
      Right(libraries)
    } else {
      checkVersion(log, config.CorrectVersions, libraries) match {
        case Left(_) if log.isSoftError => Right(rewriteLibraries(log, config, libraries))
        case Left(er)                   => Left(er)
        case Right(_)                   => Right(rewriteLibraries(log, config, libraries))
      }
    }
  }

  private def rewriteLibraries(
    log:       LoggerExtended,
    config:    SafetyConfiguration,
    libraries: Seq[ModuleID]
  ): Seq[ModuleID] = {
    log.separatorDebug("LibraryDependencyWriter.rewrite")
    log.debug(s"> Rewrite starting with ${libraries.size} libraries")

    val libraryFiltered = getLibraryFiltered(log, config, libraries)

    val (goodLibraries, libraryToEdit) = libraryFiltered.partition(_._2.isEmpty)

    log.debug(s"> Will edit ${libraryToEdit.size} libraries: ")
    libraryToEdit.map(_._1).prettyString(log, "rewriteLibraries-start")

    val output = libraryToEdit
      .map {
        case (m, thingsToRemove) =>
          thingsToRemove
            .foldLeft((m, Seq[ModuleID]())) {
              case ((module, acc), toRemove) =>
                removeBadDependencies(log, config, module, acc, toRemove)
            }
      }
      .flatMap { case (m, listModule) => listModule :+ m }
      .distinct ++ goodLibraries.map(_._1)

    log.debug(s"> After replacement, we have ${output.size} libraries:")
    output.prettyString(log, "rewriteLibraries-end")
    output
  }

  private def getLibraryFiltered(
    log:       LoggerExtended,
    config:    SafetyConfiguration,
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
    config:   SafetyConfiguration,
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
          acc ++ dependencyToInjectBack.map(removeAllDependencies(config, _)).flattenEI
        )
      case Left(ex) =>
        log.debug(s"Error for $toRemove -> Exclusion rule: $ex")
        (module, acc)
      case _ => (module, acc)
    }
  }

  private def removeAllDependencies(
    config:   SafetyConfiguration,
    moduleID: ModuleID
  ): Either[String, ModuleID] = {
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

}
