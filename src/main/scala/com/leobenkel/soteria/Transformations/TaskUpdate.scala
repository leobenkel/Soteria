package com.leobenkel.soteria.Transformations

import com.leobenkel.soteria.Config.SoteriaConfiguration
import com.leobenkel.soteria.Messages.CommonMessage._
import com.leobenkel.soteria.Messages.ErrorMessage
import com.leobenkel.soteria.Modules.Dependency
import com.leobenkel.soteria.SoteriaPluginKeys
import com.leobenkel.soteria.Utils.ImplicitModuleToString._
import com.leobenkel.soteria.Utils.LoggerExtended
import sbt.{
  ConfigRef,
  Configuration,
  ConfigurationReport,
  Def,
  Keys,
  Task,
  UpdateReport
}
import sbt.librarymanagement.ModuleID

private[Transformations] trait TaskUpdate extends CheckVersion {

  /** Does not do anything special without the debugging enable. */
  def update(
      configuration: Configuration
  ): Def.Initialize[Task[UpdateReport]] =
    Def.taskDyn {
      val log = SoteriaPluginKeys.soteriaGetLog.value
      log.separatorDebug("update")
      log.debug("> Starting Update")
      val updateReport: UpdateReport = (configuration / Keys.update).value
      val soteriaConfig: SoteriaConfiguration =
        SoteriaPluginKeys.soteriaConfig.value

      Def.task {
        checkUpdatedLibraries(
          log,
          soteriaConfig,
          configuration,
          updateReport
        )
      }
    }

  private def checkUpdatedLibraries(
      log: LoggerExtended,
      soteriaConfig: SoteriaConfiguration,
      configuration: ConfigRef,
      updateReport: UpdateReport
  ): UpdateReport = {
    val allModule: Seq[(String, Seq[Dependency])] =
      getAllModuleForConfigurations(updateReport.configurations, configuration)

    printDebug(log, allModule)

    // last check up
    val allSoteriaModule = combineModules(allModule.flatMap(_._2))

    val (oneVersion, errors) = checkTooManyVersions(log, allSoteriaModule)

    checkVersion(log, soteriaConfig.CorrectVersions, oneVersion, errors) match {
      case Left(error)    => error.consume(s => log.fail(s))
      case Right(success) => success.consume(s => log.info(s))
    }

    updateReport
  }

  private def checkTooManyVersions(
      log: LoggerExtended,
      loadedModules: Seq[Dependency]
  ): (Seq[ModuleID], ErrorMessage) = {
    log.separatorDebug("TaskUpdate.checkTooManyVersions")
    val tooManyVersions = loadedModules.filter(_.tooManyVersions)
    val oneVersion =
      loadedModules.filter(_.toModuleID.isRight).map(_.toModuleID.right.get)

    val errors = tooManyVersions
      .map(m => s"${m.toString} has more than one version")
      .toError("Found libraries with more than one version")

    (oneVersion, errors)
  }

  private def getAllModuleForConfigurations(
      configurations: Seq[ConfigurationReport],
      targetConfiguration: ConfigRef
  ): Seq[(String, Seq[Dependency])] =
    configurations
      .filter(_.configuration == targetConfiguration)
      .map { c =>
        (
          c.configuration.name,
          combineModules(c.allModules.map(Dependency(_)))
        )
      }

  private def combineModules(allModule: Seq[Dependency]): Seq[Dependency] =
    allModule
      .groupBy(_.key)
      .map {
        case (_, cModules) => cModules.reduce((l, r) => (l |+| r).right.get)
      }
      .toSeq
      .sortBy(_.key)

  private def printDebug(
      log: LoggerExtended,
      allModule: Seq[(String, Seq[Dependency])]
  ): Unit = {
    val header = "Here are all categories fetch"
    log.debug(s"> $header (${allModule.size}): ")
    allModule.foreach {
      case (category, modules) =>
        log.debug(s"> For category '$category' (${modules.size}): ")
        modules.prettyString(log, "checkUpdatedLibraries")
    }
  }

  object ZTestOnlyTaskUpdate {
    @inline def combineModulesTest(
        allModule: Seq[Dependency]
    ): Seq[Dependency] = combineModules(allModule)

    @inline def checkTooManyVersionsTest(
        log: LoggerExtended,
        loadedModules: Seq[Dependency]
    ): (Seq[ModuleID], ErrorMessage) = checkTooManyVersions(log, loadedModules)

    @inline def printDebugTest(
        log: LoggerExtended,
        allModule: Seq[(String, Seq[Dependency])]
    ): Unit = printDebug(log, allModule)
  }
}
