package com.leobenkel.soteria.Transformations

import com.leobenkel.soteria.Config.SoteriaConfiguration
import com.leobenkel.soteria.Modules.Dependency
import com.leobenkel.soteria.SoteriaPluginKeys
import com.leobenkel.soteria.Utils.ImplicitModuleToString._
import com.leobenkel.soteria.Utils.LoggerExtended
import sbt.librarymanagement.ModuleID
import sbt.{ConfigRef, Configuration, ConfigurationReport, Def, Keys, Task, UpdateReport}

private[Transformations] trait TaskCheckDependencies {

  /**
    * Does not do anything special without the debugging enable.
    */
  def checkDependencies(
    configuration: Configuration
  ): Def.Initialize[Task[SoteriaConfiguration]] = {
    Def.taskDyn {
      val log = SoteriaPluginKeys.soteriaGetLog.value
      log.separatorDebug("update")
      log.debug("> Starting Update")
      val updateReport = (configuration / Keys.update).value
      val debugValue:    Option[ModuleID] = SoteriaPluginKeys.soteriaDebugModule.value
      val soteriaConfig: SoteriaConfiguration = SoteriaPluginKeys.soteriaConfig.value

      Def.task {
        debugValue.fold(soteriaConfig)(
          checkUpdatedLibraries(
            log = log,
            soteriaConfig = soteriaConfig,
            configuration = configuration,
            updateReport = updateReport
          )
        )
      }
    }
  }

  private def checkUpdatedLibraries(
    log:           LoggerExtended,
    soteriaConfig: SoteriaConfiguration,
    configuration: ConfigRef,
    updateReport:  UpdateReport
  )(
    debugValue: ModuleID
  ): SoteriaConfiguration = {
    val modulesFromCompilation: Seq[(String, Seq[Dependency])] =
      getAllModuleForConfigurations(updateReport.configurations, configuration)
    val debugModule = Dependency(debugValue)
    printDebug(log, debugModule, modulesFromCompilation)

    debugPrintScala(log, soteriaConfig, debugModule, modulesFromCompilation.flatMap(_._2))
  }

  private def debugPrintScala(
    log:                   LoggerExtended,
    soteriaConfig:         SoteriaConfiguration,
    debugModule:           Dependency,
    moduleFromCompilation: Seq[Dependency]
  ): SoteriaConfiguration = {
    val debugModuleWithKnowledge = soteriaConfig.AllModules
      .find(_ === debugModule)
      .getOrElse(debugModule)

    val newConfig = consolidateDangersDebugModule(
      soteriaConfig = soteriaConfig,
      allModule = moduleFromCompilation,
      debugModule = debugModuleWithKnowledge
    )

    log.fail("You cannot compile when 'soteriaDebugModule' is set.")

    newConfig
  }

  private def consolidateDangersDebugModule(
    soteriaConfig: SoteriaConfiguration,
    allModule:     Seq[Dependency],
    debugModule:   Dependency
  ): SoteriaConfiguration = {
    val modulesFromBuild = modulesFromBuildWithKnowledge(soteriaConfig, allModule)
    writeResultJsonToOutputFile(soteriaConfig, modulesFromBuild, debugModule)
  }

  private def modulesFromBuildWithKnowledge(
    soteriaConfig: SoteriaConfiguration,
    allModule:     Seq[Dependency]
  ): Seq[Dependency] = {
    val dependenciesFromBuild: Seq[Dependency] = allModule
      .groupBy(_.key)
      .map {
        case (_, cModules) => cModules.reduce((l, r) => (l |+| r).right.get)
      }
      .toSeq
      .sortBy(_.key)

    for {
      moduleWeKnowOf              <- soteriaConfig.NeedOverridden
      moduleFromBuildThanWeKnowOf <- dependenciesFromBuild
      if moduleWeKnowOf === moduleFromBuildThanWeKnowOf
    } yield {
      (moduleFromBuildThanWeKnowOf |+| moduleWeKnowOf).right.get
    }
  }

  private def writeResultJsonToOutputFile(
    config:                       SoteriaConfiguration,
    moduleFromBuildWithKnowledge: Seq[Dependency],
    debugModule:                  Dependency
  ): SoteriaConfiguration = {
    val newDependency: Dependency = debugModule.copy(
      dependenciesToRemove = moduleFromBuildWithKnowledge.filter(_ =!= debugModule).map(_.nameObj)
    )

    config.replaceModule(newDependency)
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

  private def printDebug(
    log:        LoggerExtended,
    debugValue: Dependency,
    allModule:  Seq[(String, Seq[Dependency])]
  ): Unit = {
    val header = s"The module ${debugValue.toString} have fetch categories"

    log.debug(s"> $header (${allModule.size}): ")

    allModule.foreach {
      case (category, modules) =>
        log.debug(s"> For category '$category' (${modules.size}): ")
        modules.prettyString(log, "checkUpdatedLibraries")
    }
  }
}
