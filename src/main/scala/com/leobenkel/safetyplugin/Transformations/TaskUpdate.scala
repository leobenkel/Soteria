package com.leobenkel.safetyplugin.Transformations

import com.leobenkel.safetyplugin.Config.SafetyConfiguration
import com.leobenkel.safetyplugin.Messages.CommonMessage._
import com.leobenkel.safetyplugin.Modules.Dependency
import com.leobenkel.safetyplugin.SafetyPluginKeys
import com.leobenkel.safetyplugin.Utils.{LoggerExtended, SafetyLogger}
import com.leobenkel.safetyplugin.Utils.ImplicitModuleToString._
import sbt.{ConfigRef, Configuration, ConfigurationReport, Def, Keys, Task, UpdateReport}

private[Transformations] trait TaskUpdate extends CheckVersion {

  /**
    * Does not do anything special without the debugging enable.
    */
  def update(configuration: Configuration): Def.Initialize[Task[sbt.UpdateReport]] = {
    Def.taskDyn {
      val log = SafetyPluginKeys.safetyGetLog.value
      log.separatorDebug("update")
      log.debug("> Starting Update")
      val updateReport = (configuration / Keys.update).value
      val printScalaCode = SafetyPluginKeys.safetyDebugPrintScalaCode.value
      val debugValue = SafetyPluginKeys.safetyDebugModule.value
      val safetyConfig: SafetyConfiguration = SafetyPluginKeys.safetyConfig.value

      Def.task {
        checkUpdatedLibraries(
          log,
          safetyConfig,
          configuration,
          updateReport,
          debugValue,
          printScalaCode
        )
      }
    }
  }

  private def checkUpdatedLibraries(
    log:            SafetyLogger,
    safetyConfig:   SafetyConfiguration,
    configuration:  ConfigRef,
    updateReport:   UpdateReport,
    debugValue:     Option[(String, String)],
    printScalaCode: Boolean
  ): sbt.UpdateReport = {
    val allModule: Seq[(String, Seq[Dependency])] =
      getAllModuleForConfigurations(updateReport.configurations, configuration)

    printDebug(log, debugValue, allModule)

    if (debugValue.isDefined) {
      debugPrintScala(log, safetyConfig, printScalaCode, debugValue.get, allModule.flatMap(_._2))
    } else {
      // last check up
      val allSafetyModule = combineModules(allModule)

      lastCheckUp(log, safetyConfig, allSafetyModule) match {
        case Left(error)    => error.consume(log.fail)
        case Right(success) => success.consume(log.infoNotLazy)
      }
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
    checkVersion(log, config, oneVersion, errors)
  }

  private def debugPrintScala(
    log:            SafetyLogger,
    safetyConfig:   SafetyConfiguration,
    printScalaCode: Boolean,
    debugValue:     (String, String),
    allModule:      Seq[Dependency]
  ): Unit = {
    val (org, name) = debugValue
    val debugModule = Dependency(org, name)

    if (printScalaCode) {
      val allModuleOnly: Seq[Dependency] = allModule
        .groupBy(_.key)
        .map {
          case (_, cModules) =>
            cModules.reduce((l, r) => (l |+| r).right.get)
        }
        .toSeq
        .sortBy(_.key)

      val dangerModules = for {
        dangerModule <- safetyConfig.AllModules
        module       <- allModuleOnly if dangerModule === module
      } yield {
        module
      }

      val allDangerModule: String = dangerModules
        .sortBy(m => m.key)
        .map(m => s"""ModuleNoVersion("${m.organization}","${m.name}", exactName = true)""")
        .mkString(",\n")
      val modOrg = debugModule.organization
      val modName = debugModule.name
      log.info(s"""
           | ModuleNoVersion("$modOrg", "$modName", exactName = true) -> Seq(
           | $allDangerModule
           | )
              """.stripMargin)
    }

    sys.error("You cannot compile when 'safetyDebugModule' is set.")
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
    log:        SafetyLogger,
    debugValue: Option[(String, String)],
    allModule:  Seq[(String, Seq[Dependency])]
  ): Unit = {
    val logger: String => Unit = debugValue match {
      case None    => log.debug(_)
      case Some(_) => log.info(_)
    }

    val header = debugValue.fold("Here are all categories fetch") {
      case (org, name) => s"The module ${Dependency(org, name).toString} have fetch categories"
    }
    logger(s"> $header (${allModule.size}): ")
    allModule.foreach {
      case (category, modules) =>
        logger(s"> For category '$category' (${modules.size}): ")
        modules.prettyString(log, "checkUpdatedLibraries")
    }
  }
}
