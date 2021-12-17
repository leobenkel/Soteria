package com.leobenkel.soteria.Transformations

import com.leobenkel.soteria.Config.SoteriaConfiguration
import com.leobenkel.soteria.Messages.CommonMessage._
import com.leobenkel.soteria.Messages.Errors
import com.leobenkel.soteria.Modules.{Dependency, NameOfModule}
import com.leobenkel.soteria.SoteriaPluginKeys
import com.leobenkel.soteria.Utils.EitherUtils._
import com.leobenkel.soteria.Utils.ImplicitModuleToString._
import com.leobenkel.soteria.Utils.LoggerExtended
import sbt._

private[Transformations] trait TaskAllDependencies
    extends CheckVersion with TaskLibraryDependencies with TaskVersions {

  /**
   * This will rewrite the dependencies. When debugging is enable, this will also remove all
   * libraries but the tested one.
   */
  def allDependencies(conf: Option[Configuration]): Def.Initialize[Task[Seq[ModuleID]]] =
    Def.taskDyn {
      val log = SoteriaPluginKeys.soteriaGetLog.value
      val libraries = Keys.allDependencies.value
      val lib = libraryDependencies(conf).value
      val debugValue = SoteriaPluginKeys.soteriaDebugModule.value
      val soteriaConfig: SoteriaConfiguration =
        SoteriaPluginKeys.soteriaConfig.value

      val scalaVersion = scalaVersionExec().value
      val sbtVersion = sbtVersionExec().value

      Def.task {
        execAllDependencies(
          log = log,
          soteriaConfig = soteriaConfig,
          libraries = (libraries ++ lib).toSet.toList,
          debugValue = debugValue
        )
      }
    }

  private def execAllDependencies(
    log:           LoggerExtended,
    soteriaConfig: SoteriaConfiguration,
    libraries:     Seq[ModuleID],
    debugValue:    Option[ModuleID]
  ): Seq[sbt.ModuleID] = {
    log.separatorDebug("allDependencies")
    log.debug(s"> Start 'allDependencies' with ${libraries.size} libraries.")
    libraries.prettyString(log, "allDependencies")

    debugValue
      .map(Dependency(_))
      .map { debugModule =>
        log.info(s"> Debug mode, filter with ${debugModule.toString}")
        filterLibraries(log, libraries, debugModule)
      }
      .getOrElse(rewriteLibAndVersionCheck(log, soteriaConfig, libraries)) match {
      case Left(errors) =>
        errors.consume(log.fail(_))
        Seq.empty
      case Right(rewroteLibraries) =>
        log.info(
          s"> 'allDependencies' have ${rewroteLibraries.size} libraries."
        )
        rewroteLibraries
    }
  }

  private val goodLibraries: Seq[String] = Seq("org.scala-lang")

  /**
   * Remove all but the wanted library.
   *
   * @return
   */
  private def filterLibraries(
    log:        LoggerExtended,
    libraries:  Seq[ModuleID],
    whatToKeep: Dependency
  ): Either[Errors, Seq[ModuleID]] = {
    val output = libraries
      .filter(m => goodLibraries.contains(m.organization) || whatToKeep === m)
      .map(_.withConfigurations(None))

    log.debug(
      s"Found ${output.size} matching libraries with ${libraries.size} input libraries:"
    )
    output.prettyString(log, "filter")

    if (output.exists(m => whatToKeep === m)) Right(output)
    else
      Left(
        s"Could not find ${whatToKeep.toString} among the input libraries".asErrors
      )
  }

  /**
   * This method rewrite the libraries to make sure the risky dependencies are not fetch and
   * replaced.
   *
   * @param libraries
   *   the libraries fetch by the build, coming from [[sbt.Keys.allDependencies]]
   *
   * @return
   */
  private def rewriteLibAndVersionCheck(
    log:       LoggerExtended,
    config:    SoteriaConfiguration,
    libraries: Seq[ModuleID]
  ): Either[Errors, Seq[ModuleID]] =
    checkVersion(log, config.CorrectVersions, libraries) match {
      case Right(_) => Right(rewriteLibraries(log, config, libraries))
      case Left(_) if log.isSoftError =>
        Right(rewriteLibraries(log, config, libraries))
      case Left(er) => Left(er)
    }

  private def rewriteLibraries(
    log:       LoggerExtended,
    config:    SoteriaConfiguration,
    libraries: Seq[ModuleID]
  ): Seq[ModuleID] = {
    log.separatorDebug("TaskAllDependencies.rewriteLibraries")
    log.debug(s"> Rewrite starting with ${libraries.size} libraries")

    val libraryFiltered =
      getLibraryFiltered(log, config.PackageKnownRiskDependencies, libraries)

    val (goodLibraries, libraryToEdit) = libraryFiltered.partition(_._2.isEmpty)

    log.debug(s"> Will edit ${libraryToEdit.size} libraries: ")
    libraryToEdit.map(_._1).prettyString(log, "rewriteLibraries-start")

    val output = excludeBadDependencies(
      log,
      libraryToEdit,
      config.NeedToBeReplaced
    ) ++ goodLibraries.map(_._1)

    log.debug(s"> After replacement, we have ${output.size} libraries:")
    output.prettyString(log, "rewriteLibraries-end")
    output
  }

  private def excludeBadDependencies(
    log:              LoggerExtended,
    libraryToEdit:    Seq[(ModuleID, Seq[NameOfModule])],
    needToBeReplaced: Seq[Dependency]
  ): Seq[ModuleID] = {
    libraryToEdit
      .map {
        case (m, thingsToRemove) =>
          thingsToRemove.foldLeft((m, Seq[ModuleID]())) {
            case ((module, acc), toRemove) =>
              removeBadDependencies(
                log = log,
                needToBeReplaced = needToBeReplaced,
                module = module,
                acc = acc,
                toRemove = toRemove
              )
          }
      }
      .flatMap { case (m, listModule) => listModule :+ m }
      .groupBy(m => (m.organization, m.name))
      .map {
        case (_, modules) =>
          val module = modules.maxBy(_.exclusions.length)
          val exclusions = modules.map(_.exclusions).reduce(_ ++ _).distinct
          module.withExclusions(exclusions)
      }
      .toSeq
      .distinct
  }

  private def getLibraryFiltered(
    log:                          LoggerExtended,
    packageKnownRiskDependencies: Map[Dependency, Seq[NameOfModule]],
    libraries:                    Seq[ModuleID]
  ): Seq[(ModuleID, Seq[NameOfModule])] =
    libraries.map { m =>
      val depToRemove =
        packageKnownRiskDependencies.filterKeys(_ === m).values.flatten.toSeq.distinct
      log.debug(
        s"For ${m.prettyString}: found ${depToRemove.length} lib to remove"
      )

      (m, depToRemove)
    }

  private def removeBadDependencies(
    log:              LoggerExtended,
    needToBeReplaced: Seq[Dependency],
    module:           ModuleID,
    acc:              Seq[ModuleID],
    toRemove:         NameOfModule
  ): (ModuleID, Seq[ModuleID]) = {
    toRemove.exclusionRule match {
      case Right(er) =>
        val depsToReplace: Option[ModuleID] = needToBeReplaced
          .filter(_.key == toRemove.key)
          .filter(_.overrideIsEnough)
          .map(_.toModuleID)
          .flattenEI
          .map(removeAllDependencies(needToBeReplaced, _))
          .flattenEI
          .headOption

        log.debug(
          s"> For ${module.prettyString} - Exclude ${toRemove.toString}"
        )

        depsToReplace match {
          case Some(dep) =>
            log.debug(s">> replacing with: ${dep.prettyString}.")
          case None => log.debug(s">> removing entirely.")
        }

        (module.excludeAll(er), depsToReplace.map(acc :+ _).getOrElse(acc))
      case Left(ex) =>
        log
          .setSoftError(true)
          .fail(s"""> For ${module.prettyString} - Exclude ${toRemove.toString}
               |>> Error for $toRemove -> Exclusion rule: $ex""".stripMargin)
        (module, acc)
    }
  }

  private def removeAllDependencies(
    needToBeReplaced: Seq[Dependency],
    moduleID:         ModuleID
  ): Either[String, ModuleID] =
    needToBeReplaced.foldLeft(Right(moduleID): Either[String, ModuleID]) {
      case (m, toRemove) =>
        m.flatMap { module =>
          // to not remove itself from itself
          if (toRemove =!= module)
            toRemove.exclusionRule.right.map(module.excludeAll(_))
          else Right(module)
        }
    }

  object ZTestOnlyTaskAllDependencies {
    def filterLibrariesTest(
      log:        LoggerExtended,
      libraries:  Seq[ModuleID],
      whatToKeep: Dependency
    ): Either[Errors, Seq[ModuleID]] = filterLibraries(log, libraries, whatToKeep)

    def getLibraryFilteredTest(
      log:                          LoggerExtended,
      packageKnownRiskDependencies: Map[Dependency, Seq[NameOfModule]],
      libraries:                    Seq[ModuleID]
    ): Seq[(ModuleID, Seq[NameOfModule])] =
      getLibraryFiltered(log, packageKnownRiskDependencies, libraries)

    def removeBadDependenciesTest(
      log:              LoggerExtended,
      needToBeReplaced: Seq[Dependency],
      module:           ModuleID,
      acc:              Seq[ModuleID],
      toRemove:         NameOfModule
    ): (ModuleID, Seq[ModuleID]) =
      removeBadDependencies(
        log,
        needToBeReplaced,
        module,
        acc,
        toRemove
      )

    def execAllDependenciesTest(
      log:           LoggerExtended,
      soteriaConfig: SoteriaConfiguration,
      libraries:     Seq[ModuleID],
      debugValue:    Option[ModuleID]
    ): Seq[sbt.ModuleID] =
      execAllDependencies(
        log,
        soteriaConfig,
        libraries,
        debugValue
      )

    def rewriteLibAndVersionCheckTest(
      log:       LoggerExtended,
      config:    SoteriaConfiguration,
      libraries: Seq[ModuleID]
    ): Either[Errors, Seq[ModuleID]] =
      rewriteLibAndVersionCheck(
        log,
        config,
        libraries
      )

    def rewriteLibrariesTest(
      log:       LoggerExtended,
      config:    SoteriaConfiguration,
      libraries: Seq[ModuleID]
    ): Seq[ModuleID] = rewriteLibraries(log, config, libraries)

    def excludeBadDependenciesTest(
      log:              LoggerExtended,
      libraryToEdit:    Seq[(ModuleID, Seq[NameOfModule])],
      needToBeReplaced: Seq[Dependency]
    ): Seq[ModuleID] = excludeBadDependencies(log, libraryToEdit, needToBeReplaced)
  }
}
