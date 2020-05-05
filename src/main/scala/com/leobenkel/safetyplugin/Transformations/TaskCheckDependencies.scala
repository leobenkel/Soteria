package com.leobenkel.safetyplugin.Transformations

import com.leobenkel.safetyplugin.Config.SafetyConfiguration
import com.leobenkel.safetyplugin.Modules.Dependency
import com.leobenkel.safetyplugin.SafetyPluginKeys
import com.leobenkel.safetyplugin.Utils.ImplicitModuleToString._
import com.leobenkel.safetyplugin.Utils.LoggerExtended
import sbt.librarymanagement.ModuleID
import sbt.{ConfigRef, Configuration, ConfigurationReport, Def, Keys, Task, UpdateReport}

private[Transformations] trait TaskCheckDependencies {

  /**
    * Does not do anything special without the debugging enable.
    */
  def checkDependencies(configuration: Configuration): Def.Initialize[Task[SafetyConfiguration]] = {
    Def.taskDyn {
      val log = SafetyPluginKeys.safetyGetLog.value
      log.separatorDebug("update")
      log.debug("> Starting Update")
      val updateReport = (configuration / Keys.update).value
      val debugValue:   Option[ModuleID] = SafetyPluginKeys.safetyDebugModule.value
      val safetyConfig: SafetyConfiguration = SafetyPluginKeys.safetyConfig.value

      Def.task {
        debugValue.fold(safetyConfig)(
          checkUpdatedLibraries(
            log = log,
            safetyConfig = safetyConfig,
            configuration = configuration,
            updateReport = updateReport
          )
        )
      }
    }
  }

  private def checkUpdatedLibraries(
    log:           LoggerExtended,
    safetyConfig:  SafetyConfiguration,
    configuration: ConfigRef,
    updateReport:  UpdateReport
  )(
    debugValue: ModuleID
  ): SafetyConfiguration = {
    val modulesFromCompilation: Seq[(String, Seq[Dependency])] =
      getAllModuleForConfigurations(updateReport.configurations, configuration)
    val debugModule = Dependency(debugValue)
    printDebug(log, debugModule, modulesFromCompilation)

    debugPrintScala(log, safetyConfig, debugModule, modulesFromCompilation.flatMap(_._2))
  }

  private def debugPrintScala(
    log:                   LoggerExtended,
    safetyConfig:          SafetyConfiguration,
    debugModule:           Dependency,
    moduleFromCompilation: Seq[Dependency]
  ): SafetyConfiguration = {
    val debugModuleWithKnowledge = safetyConfig.AllModules
      .find(_ === debugModule)
      .getOrElse(debugModule)

    val newConfig = consolidateDangersDebugModule(
      safetyConfig = safetyConfig,
      allModule = moduleFromCompilation,
      debugModule = debugModuleWithKnowledge
    )

    log.fail("You cannot compile when 'safetyDebugModule' is set.")

    newConfig
  }

  private def consolidateDangersDebugModule(
    safetyConfig: SafetyConfiguration,
    allModule:    Seq[Dependency],
    debugModule:  Dependency
  ): SafetyConfiguration = {
    val modulesFromBuild = modulesFromBuildWithKnowledge(safetyConfig, allModule)
    writeResultJsonToOutputFile(safetyConfig, modulesFromBuild, debugModule)
  }

  private def modulesFromBuildWithKnowledge(
    safetyConfig: SafetyConfiguration,
    allModule:    Seq[Dependency]
  ): Seq[Dependency] = {
    val dependenciesFromBuild: Seq[Dependency] = allModule
      .groupBy(_.key)
      .map {
        case (_, cModules) => cModules.reduce((l, r) => (l |+| r).right.get)
      }
      .toSeq
      .sortBy(_.key)

    for {
      moduleWeKnowOf              <- safetyConfig.NeedOverridden
      moduleFromBuildThanWeKnowOf <- dependenciesFromBuild
      if moduleWeKnowOf === moduleFromBuildThanWeKnowOf
    } yield {
      (moduleFromBuildThanWeKnowOf |+| moduleWeKnowOf).right.get
    }
  }

  private def writeResultJsonToOutputFile(
    config:                       SafetyConfiguration,
    moduleFromBuildWithKnowledge: Seq[Dependency],
    debugModule:                  Dependency
  ): SafetyConfiguration = {
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
