package com.leobenkel.safetyplugin.Transformations

import com.leobenkel.safetyplugin.Config.SafetyConfiguration
import com.leobenkel.safetyplugin.Modules.Dependency
import com.leobenkel.safetyplugin.SafetyPluginKeys
import com.leobenkel.safetyplugin.Utils.Json.JsonDecode
import com.leobenkel.safetyplugin.Utils.LoggerExtended
import sbt._
import sbt.internal.util.complete.Parser
import sbt.librarymanagement.ModuleID

private[Transformations] trait TaskDebugModule {
  private val CommandName:    String = "safetyDebugModuleWithCode"
  private val CommandNameAll: String = "safetyDebugAllModules"

  @transient lazy private val parseModule: Parser[Dependency] = {
    import sbt._
    import complete.DefaultParsers._

    implicit class ParserNoQuote(p: Parser[String]) {
      val ensureNoQuotes: Parser[String] = {
        p.map {
          case n if n.contains('"') => throw new Exception()
          case n                    => n
        }.failOnException
      }
    }

    val singlePercent: Parser[String] = Space ~> literal("%").examples("%") <~ Space
    val doublePercent: Parser[String] = Space ~> literal("%%").examples("%%") <~ Space
    val percentParser: Parser[String] = singlePercent | doublePercent
    val orgParser = token(StringBasic, "<organization>").ensureNoQuotes
    val artifactParser = token(StringBasic, "<artifact>").ensureNoQuotes
    val revisionParser = token(StringBasic, "<revision>").ensureNoQuotes

    (OptSpace ~> orgParser ~
      (percentParser ~ artifactParser) ~
      (singlePercent ~> revisionParser) <~ OptSpace)
      .map {
        case ((organization, (percent, artifact)), revision) =>
          Dependency(
            organization,
            artifact,
            revision,
            needDoublePercent = percent.contains("%%")
          )
      }
  }

  private def executeDebugModuleCommand(
    log:     LoggerExtended,
    module:  Either[String, Dependency],
    execute: ModuleID => SafetyConfiguration
  ): Option[SafetyConfiguration] = {
    module match {
      case Right(module: Dependency) =>
        val moduleId = module.toModuleID

        moduleId match {
          case Right(m) => Some(execute(m))
          case Left(e) =>
            log.fail(s"Module '$module' is invalid: $e")
            None
        }
      case Left(e) =>
        log.fail(e)
        None
    }
  }

  def executeCompilation(
    state:  State,
    config: SafetyConfiguration,
    m:      ModuleID
  ): (State, SafetyConfiguration) = {
    val newState = Project
      .extract(state).appendWithoutSession(
        Seq(
          Keys.libraryDependencies += m,
          SafetyPluginKeys.safetyDebugModule    := Some(m.organization, m.name),
          SafetyPluginKeys.safetySoft           := true,
          SafetyPluginKeys.safetyConfig         := config
        ),
        state
      )

    Project.extract(newState).runTask(Test / SafetyPluginKeys.safetyBuildConfig, newState)
  }

  def debugModuleCommand: Command = {
    Command
      .args(CommandName, "") { (state, args) =>
        val result: Either[String, Dependency] = Parser.parse(args.mkString(" ").trim, parseModule)
        val config = Project.extract(state).get(SafetyPluginKeys.safetyConfig)
        val log = Project.extract(state).get(SafetyPluginKeys.safetyGetLog)

        val fullConfigOpt = executeDebugModuleCommand(
          log = log,
          module = result,
          execute = m => { executeCompilation(state, config, m)._2 }
        )

        fullConfigOpt match {
          case None => ()
          case Some(fullConfig) =>
            log.info("Output configuration: ")
            log.info("")

            val encoded: Either[String, String] = JsonDecode.encode(fullConfig)

            encoded match {
              case Left(error) => log.error(s"Failed to encode dependency: $error")
              case Right(json) => log.info(json)
            }
        }

        state
      }
  }

  def debugAllModuleCommand: Command = {
    Command
      .args(CommandNameAll, "") { (state, _) =>
        val log = Project.extract(state).get(SafetyPluginKeys.safetyGetLog)
        val libraries = Project.extract(state).get(Keys.libraryDependencies)
        val config = Project.extract(state).get(SafetyPluginKeys.safetyConfig)

        val allModules: Set[ModuleID] = config.AllModuleID ++ libraries

        val (_, fullConfig) = allModules.foldLeft((state, config)) {
          case ((currentState, conf), module) =>
            executeCompilation(currentState, conf, module)
        }

        log.info("Final configuration: ")
        log.info("")

        val encoded: Either[String, String] = JsonDecode.encode(fullConfig)

        encoded match {
          case Left(error) => log.error(s"Failed to encode dependency: $error")
          case Right(json) => log.info(json)
        }

        state
      }
  }

  object ZTestOnlyTaskDebugModule {
    val parser = parseModule

    def execute(
      log:     LoggerExtended,
      module:  Either[String, Dependency],
      execute: ModuleID => SafetyConfiguration
    ): Option[SafetyConfiguration] = {
      executeDebugModuleCommand(log, module, execute)
    }
  }
}
