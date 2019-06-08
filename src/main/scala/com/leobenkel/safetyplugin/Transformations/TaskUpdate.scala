package com.leobenkel.safetyplugin.Transformations

import com.leobenkel.safetyplugin.Config.SafetyConfiguration
import com.leobenkel.safetyplugin.Messages.CommonMessage._
import com.leobenkel.safetyplugin.Modules.Dependency
import com.leobenkel.safetyplugin.SafetyPluginKeys
import com.leobenkel.safetyplugin.Utils.ImplicitModuleToString._
import com.leobenkel.safetyplugin.Utils.LoggerExtended
import sbt.{ConfigRef, Configuration, ConfigurationReport, Def, Keys, Task, UpdateReport}

private[Transformations] trait TaskUpdate extends CheckVersion {
  /**
    * Does not do anything special without the debugging enable.
    */
  def update(configuration: Configuration): Def.Initialize[Task[UpdateReport]] = {
    Def.taskDyn {
      val log = SafetyPluginKeys.safetyGetLog.value
      log.separatorDebug("update")
      log.debug("> Starting Update")
      val updateReport: UpdateReport = (configuration / Keys.update).value
      val safetyConfig: SafetyConfiguration = SafetyPluginKeys.safetyConfig.value

      Def.task {
        checkUpdatedLibraries(
          log,
          safetyConfig,
          configuration,
          updateReport
        )
      }
    }
  }

  private def checkUpdatedLibraries(
    log:           LoggerExtended,
    safetyConfig:  SafetyConfiguration,
    configuration: ConfigRef,
    updateReport:  UpdateReport
  ): UpdateReport = {
    val allModule: Seq[(String, Seq[Dependency])] =
      getAllModuleForConfigurations(updateReport.configurations, configuration)

    printDebug(log, allModule)

    // last check up
    val allSafetyModule = combineModules(allModule)

    lastCheckUp(log, safetyConfig, allSafetyModule) match {
      case Left(error)    => error.consume(s => log.fail(s))
      case Right(success) => success.consume(s => log.info(s))
    }

    updateReport
  }

  private def lastCheckUp(
    log:       LoggerExtended,
    config:    SafetyConfiguration,
    allModule: Seq[Dependency]
  ): ResultMessages = {
    log.separatorDebug("LibraryDependencyWriter.lastCheckUp")
    val tooManyVersions = allModule.filter(_.tooManyVersions)
    val oneVersion = allModule.filter(_.toModuleID.isRight).map(_.toModuleID.right.get)

    val errors = tooManyVersions
      .map(m => s"More than one version of: ${m.toString} - [${m.versions.mkString(", ")}]")
      .toError("Found libraries with more than one version")
    checkVersion(log, config.CorrectVersions, oneVersion, errors)
  }

  private def getAllModuleForConfigurations(
    configurations:      Seq[ConfigurationReport],
    targetConfiguration: ConfigRef
  ): Seq[(String, Seq[Dependency])] = {
    configurations
      .filter(_.configuration == targetConfiguration)
      .map { c =>
        (
          c.configuration.name,
          c.allModules
            .map(Dependency(_))
            .groupBy(_.key)
            .map { case (_, cModules) => cModules.reduce((l, r) => (l |+| r).right.get) }
            .toSeq
            .sortBy(_.key)
        )
      }
  }

  private def combineModules(allModule: Seq[(String, Seq[Dependency])]): Seq[Dependency] = {
    allModule
      .flatMap(_._2)
      .groupBy(_.key)
      .map { case (_, cModules) => cModules.reduce((l, r) => (l |+| r).right.get) }
      .toSeq
      .sortBy(_.key)
  }

  private def printDebug(
    log:       LoggerExtended,
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
}
