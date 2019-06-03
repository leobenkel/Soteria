package com.leobenkel.safetyplugin

import com.leobenkel.safetyplugin.Config._
import com.leobenkel.safetyplugin.Modules.Dependency
import com.leobenkel.safetyplugin.SafetyPluginKeys._
import com.leobenkel.safetyplugin.Utils.ImplicitModuleToString._
import com.leobenkel.safetyplugin.Utils.SafetyLogger
import sbt.internal.util.complete.Parser
import sbt.{Def, _}
import sbtassembly._
import com.leobenkel.safetyplugin.Utils.EitherUtils._

private[safetyplugin] object SafetyExecutionLogic {
  def safetyGetLogExec(): Def.Initialize[SafetyLogger] = {
    Def.settingDyn {
      val log = SafetyPluginKeys.safetyGetLog.value
      val level = safetyLogLevel.value
      val softError = safetySoft.value
      Def.setting {
        log
          .setLevel(level)
          .setSoftError(softError)
      }
    }
  }

  def safetyConfigurationExec(): Def.Initialize[Config.SafetyConfiguration] = {
    Def.settingDyn {
      val log = SafetyPluginKeys.safetyGetLog.value
      val path = safetyConfPath.value

      Def.setting {
        val conf = Config.ConfigurationParser(log, path)
        conf.getConf
      }
    }
  }

  def defaultAssemblyOptionExec: Def.Initialize[Task[AssemblyOption]] = {
    Def.taskDyn {
      Def.task {
        val s = Keys.streams.value
        AssemblyOption(
          assemblyDirectory = s.cacheDirectory / "assembly",
          includeBin = (Keys.packageBin / AssemblyKeys.assembleArtifact).value,
          includeScala = (AssemblyKeys.assemblyPackageScala / AssemblyKeys.assembleArtifact).value,
          includeDependency = (AssemblyKeys.assemblyPackageDependency /
            AssemblyKeys.assembleArtifact).value,
          mergeStrategy = (AssemblyKeys.assembly / AssemblyKeys.assemblyMergeStrategy).value,
          excludedJars = (AssemblyKeys.assembly / AssemblyKeys.assemblyExcludedJars).value,
          excludedFiles = Assembly.defaultExcludedFiles,
          cacheOutput = true,
          cacheUnzip = true,
          appendContentHash = false,
          prependShellScript = None,
          maxHashLength = None,
          shadeRules = (AssemblyKeys.assembly / AssemblyKeys.assemblyShadeRules).value,
          level = (AssemblyKeys.assembly / Keys.logLevel).value
        )
      }
    }
  }

  def safetyAssemblySettingsExec(): Def.Initialize[Task[AssemblyOption]] = {
    Def.taskDyn {
      val log = safetyGetLog.value
      val oldStrategy: String => MergeStrategy = (AssemblyKeys.assembly /
        AssemblyKeys.assemblyMergeStrategy).value
      val assemblyOption = defaultAssemblyOption.value
      log.info(s"Overriding assembly settings")

      Def.task {
        val mergeStrategy =
          (input: String) => MergeStrategyConfiguration.getMergeStrategy(input, oldStrategy)
        val shadeRule = Seq(ShadeRule.rename("com.google.common.**" -> "shade.@0").inAll)

        assemblyOption
          .copy(
            includeScala = false,
            includeDependency = true,
            shadeRules = shadeRule,
            mergeStrategy = mergeStrategy
          )
      }
    }
  }

  def extraScalacOptions(conf: Option[Configuration]): Def.Initialize[Task[Seq[String]]] = {
    Def.taskDyn {
      val log = safetyGetLog.value
      val shouldNotFail = safetySoftOnCompilerWarning.value
      val origin = conf.fold(Keys.scalacOptions)(_ / Keys.scalacOptions).value
      val configuration = SafetyPluginKeys.safetyConfig.value

      Def.task {
        log.debug(s"updating 'scalacOptions' (ShouldFail: $shouldNotFail)")
        val compilerFlags = configuration.scalaCFlags

        (
          origin ++
            compilerFlags ++
            (if (shouldNotFail) Seq.empty else Seq("-Xfatal-warnings"))
        ).distinct
      }
    }
  }

  private def debugLibraryDependencies(
    log:                 SafetyLogger,
    conf:                Option[Configuration],
    libraryDependencies: Seq[ModuleID]
  ): Unit = {
    log.separatorDebug(s"$conf / libraryDependencies")
    log.debug(s"Found ${libraryDependencies.size} libraries in 'libraryDependencies': ")
    libraryDependencies.prettyString(log, s"$conf/libraryDependencies")
  }

  private def getForbiddenVersionErrors(
    libraryDependencies: Seq[ModuleID],
    forbiddenModules:    Seq[(Dependency, String)]
  ): Seq[String] = {
    for {
      inputLib     <- libraryDependencies
      forbidModule <- forbiddenModules if forbidModule._1 === inputLib
    } yield {
      s"${inputLib.prettyString}\n   Detailed error > ${forbidModule._2}"
    }
  }

  private def getProvidedEnforcedErrors(
    libraryDependencies: Seq[ModuleID],
    mustBeProvided:      Seq[Dependency]
  ): Seq[String] = {
    for {
      providedLib <- mustBeProvided
      inputLib <- libraryDependencies
        .filterNot(_.configurations.getOrElse("").contains("test"))
        .filterNot(_.configurations.getOrElse("").contains("provided"))
      if providedLib === inputLib
    } yield {
      s"${inputLib.prettyString}\n   Detailed error > Should be marked as provided."
    }
  }

  private def processLibraryDependencies(
    log:                 SafetyLogger,
    config:              SafetyConfiguration,
    conf:                Option[Configuration],
    libraryDependencies: Seq[ModuleID]
  ): Seq[sbt.ModuleID] = {
    debugLibraryDependencies(log, conf, libraryDependencies)

    val errors = (getForbiddenVersionErrors(libraryDependencies, config.ForbiddenModules) ++
      getProvidedEnforcedErrors(libraryDependencies, config.AsProvided))
      .sortBy(identity)

    if (errors.nonEmpty) {
      log.fail(s"You have errors in your 'libraryDependencies': \n${errors.mkString("\n")}")
    }

    libraryDependencies
  }

  def libraryDependencies(conf: Option[Configuration]): Def.Initialize[Seq[ModuleID]] = {
    Def.settingDyn {
      val log = safetyGetLog.value
      val libraryDependencies =
        conf.fold(Keys.libraryDependencies)(_ / Keys.libraryDependencies).value
      val config = SafetyPluginKeys.safetyConfig.value

      Def.setting {
        processLibraryDependencies(log, config, conf, libraryDependencies)
      }
    }
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

      LibraryDependencyWriter(safetyConfig).lastCheckUp(log, allSafetyModule) match {
        case Left(error)    => error.consume(log.fail)
        case Right(success) => success.consume(log.infoNotLazy)
      }
    }
    updateReport
  }

  /**
    * Does not do anything special without the debugging enable.
    */
  def update(configuration: Configuration): Def.Initialize[Task[sbt.UpdateReport]] = {
    Def.taskDyn {
      val log = safetyGetLog.value
      log.separatorDebug("update")
      log.debug("> Starting Update")
      val updateReport = (configuration / Keys.update).value
      val printScalaCode = safetyDebugPrintScalaCode.value
      val debugValue = safetyDebugModule.value
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

  /**
    * This will rewrite the dependencies. And then rewrite the libraries with
    * exclusions and reinjection of the removed ones with the correct version. For more details,
    * look at [[LibraryDependencyWriter.rewrite]] .
    * When debugging is enable, this will also remove all libraries but the tested one.
    */
  def allDependencies(): Def.Initialize[Task[Seq[sbt.ModuleID]]] = {
    Def.taskDyn {
      val log = safetyGetLog.value
      val libraries = Keys.allDependencies.value
      val debugValue = safetyDebugModule.value
      val debugWithScala = safetyDebugWithScala.value
      val safetyConfig: SafetyConfiguration = SafetyPluginKeys.safetyConfig.value
      val libraryDependencyWriter = LibraryDependencyWriter(safetyConfig)

      Def.task {
        log.separatorDebug("allDependencies")
        log.debug(s"> Start 'allDependencies' with ${libraries.size} libraries.")
        libraries.prettyString(log, "allDependencies")

        (if (debugValue.isDefined) {
           val (org, name) = debugValue.get
           val debugModule = Dependency(org, name)
           log.info(s"> Debug mode, filter with ${debugModule.toString}")
           libraryDependencyWriter.filter(log, libraries, debugModule, debugWithScala)
         } else {
           libraryDependencyWriter.rewrite(log, libraries, debugValue.isDefined)
         }) match {
          case Left(errors) =>
            if (!log.isSoftError) errors.consume(log.fail)
            Seq.empty
          case Right(rewroteLibraries) =>
            log.info(s"> 'allDependencies' have ${rewroteLibraries.size} libraries.")
            rewroteLibraries
        }
      }
    }
  }

  @transient lazy private val parseModule: Parser[Dependency] = {
    import sbt._
    import complete.DefaultParsers._

    val percentParser: Parser[String] = token("\" % \"") | token("\" %% \"")

    (token("\"") ~> token(NotQuoted, "<organization>") ~
      (percentParser ~ token(NotQuoted, "<artifact>")) ~
      (token("\" % \"") ~> token(NotQuoted, "<revision>") <~ token("\"")))
      .map {
        case ((organization, (percent, artifact)), revision) =>
          Dependency(organization, artifact, revision, needDoublePercent = percent.contains("%%"))
      }
  }

  def debugModuleCommand: Command = {
    Command
      .args("safetyDebugModuleWithCode", "") { (state, args) =>
        val result = Parser.parse(args.mkString(" ").trim, parseModule)
        val log = Project.extract(state).get(safetyGetLog)

        result match {
          case Right(module: Dependency) =>
            val moduleId = module.toModuleID
            val orgArtifact = module.toOrganizationArtifactName

            (moduleId, orgArtifact) match {
              case (Right(m), Right(_)) =>
                val newState = Project
                  .extract(state).appendWithoutSession(
                    Seq(
                      Keys.libraryDependencies += m,
                      safetyDebugModule         := Some(m.organization, m.name),
                      safetyDebugPrintScalaCode := true
                    ),
                    state
                  )

                Project.extract(newState).runTask(Test / Keys.compile, newState)
                ()
              case _ => log.fail(s"Module '$module' with revision '${module.version}' is invalid.")
            }
          case Left(e) => log.fail(e)
        }

        state
      }
  }

  /**
    * Will check that SBT is the correct version.
    */
  def sbtVersionExec(): Def.Initialize[String] = {
    Def.settingDyn {
      val log = safetyGetLog.value
      val sbtVersion: String = Keys.sbtVersion.value
      val configuration = SafetyPluginKeys.safetyConfig.value

      Def.setting {
        val legalSbtVersion = configuration.sbtVersion
        if (sbtVersion != legalSbtVersion) {
          log.fail(s"SBT: $sbtVersion != $legalSbtVersion !!!")
        } else {
          log.debug(s"SBT: $sbtVersion (correct)")
        }

        sbtVersion
      }
    }
  }

  /**
    * Will check that Scala is the correct version.
    */
  def scalaVersionExec(): Def.Initialize[String] = {
    Def.settingDyn {
      val log = safetyGetLog.value
      val scalaVersion: String = Keys.scalaVersion.value
      val configuration = SafetyPluginKeys.safetyConfig.value

      Def.setting {
        val legalScalaVersion = configuration.scalaVersions
        if (legalScalaVersion.contains(scalaVersion)) {
          log.debug(s"Scala: $scalaVersion (correct)")
        } else {
          log.fail(s"Scala: $scalaVersion != [${legalScalaVersion.mkString(" OR ")}] !!!")
        }

        scalaVersion
      }
    }
  }

  def getAllDependencies: Def.Initialize[Seq[ModuleID]] = {
    Def.settingDyn {
      val log = safetyGetLog.value
      val scalaVersion = Keys.scalaVersion.value
      val scalaMainVersion = scalaVersion
        .split('.')
        .dropRight(1)
        .mkString(".")

      val config = SafetyPluginKeys.safetyConfig.value

      val allDependenciesTmp = config.ShouldDownload.map(_.toModuleID)

      allDependenciesTmp
        .filter(_.isLeft)
        .map(_.left.get)
        .foreach(log.debug(_))

      val allDependencies = allDependenciesTmp.flattenEI
        .filter { m =>
          if (m.name.contains("_")) {
            val nameBlocks = m.name.split("_")
            val moduleScalaVersion = nameBlocks.last
            moduleScalaVersion == scalaMainVersion
          } else {
            true
          }
        }

      val javaX = ("javax.ws.rs" % "javax.ws.rs-api" % "2.1")
        .artifacts(Artifact("javax.ws.rs-api", "jar", "jar"))

      Def.setting {
        (allDependencies :+ javaX).sortBy(m => (m.organization, m.name))
      }
    }
  }

  /**
    * Since this does not inject more libraries into the build but just override,
    * we always override the correct versions,
    * coming from [[SafetyConfiguration.DependenciesOverride]].
    */
  def dependencyOverrides(conf: Option[Configuration]): Def.Initialize[Seq[ModuleID]] = {
    Def.settingDyn {
      val log = safetyGetLog.value
      log.separatorDebug(s"$conf / dependencyOverrides")
      val originalDependencies =
        conf.fold(Keys.dependencyOverrides)(_ / Keys.dependencyOverrides).value
      val config = SafetyPluginKeys.safetyConfig.value

      Def.setting {
        log.debug(s"> Starting with ${originalDependencies.size} dependencyOverrides:")
        val newDependencyOverrides = (originalDependencies ++ config.DependenciesOverride).distinct

        if (conf.isEmpty) {
          log.info(s"> 'dependencyOverrides' have ${newDependencyOverrides.size} overrides.")
          newDependencyOverrides.prettyString(log, "dependencyOverrides")
        } else {
          log.debug(
            s"> '$conf / dependencyOverrides' have " +
              s"${newDependencyOverrides.size} overrides."
          )
        }

        newDependencyOverrides
      }
    }
  }

}
