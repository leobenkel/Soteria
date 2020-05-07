package com.leobenkel.soteria.Transformations

import com.leobenkel.soteria.Config.SoteriaConfiguration
import com.leobenkel.soteria.Modules.Dependency
import com.leobenkel.soteria.SoteriaPluginKeys
import com.leobenkel.soteria.Utils.Json.JsonDecode
import com.leobenkel.soteria.Utils.LoggerExtended
import sbt._
import sbt.internal.util.complete.Parser
import sbt.librarymanagement.ModuleID

private[Transformations] trait TaskDebugModule {
  private val CommandName:    String = "soteriaDebugModuleWithCode"
  private val CommandNameAll: String = "soteriaDebugAllModules"

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
    execute: ModuleID => SoteriaConfiguration
  ): Option[SoteriaConfiguration] = {
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
    state:        State,
    scalaVersion: String,
    config:       SoteriaConfiguration,
    m:            ModuleID
  ): (State, SoteriaConfiguration) = {
    val newState = Project
      .extract(state).appendWithoutSession(
        Seq(
          Keys.libraryDependencies += m,
          Keys.scalaVersion                    := scalaVersion,
          SoteriaPluginKeys.soteriaDebugModule := Some(m),
          SoteriaPluginKeys.soteriaSoft        := true,
          SoteriaPluginKeys.soteriaConfig      := config
        ),
        state
      )

    Project.extract(newState).runTask(Test / SoteriaPluginKeys.soteriaBuildConfig, newState)
  }

  def debugModuleCommand: Command = {
    Command
      .args(CommandName, "") { (state, args) =>
        val result: Either[String, Dependency] = Parser.parse(args.mkString(" ").trim, parseModule)
        val config = Project.extract(state).get(SoteriaPluginKeys.soteriaConfig)
        val log = Project.extract(state).get(SoteriaPluginKeys.soteriaGetLog)
        val scalaVersion = Project.extract(state).get(Keys.scalaVersion)

        val fullConfigOpt = executeDebugModuleCommand(
          log = log,
          module = result,
          execute = m => {
            executeCompilation(
              state = state,
              scalaVersion = scalaVersion,
              config = config,
              m = m
            )._2
          }
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
        val log = Project.extract(state).get(SoteriaPluginKeys.soteriaGetLog)
        val libraries = Project.extract(state).get(Keys.libraryDependencies)
        val config = Project.extract(state).get(SoteriaPluginKeys.soteriaConfig)
        val scalaVersions = Project.extract(state).get(Keys.crossScalaVersions)
        val scalaVersion = Project.extract(state).get(Keys.scalaVersion)

        val allScalaVersions = (scalaVersions :+ scalaVersion).toSet

        val (_, fullConfig) = allScalaVersions.foldLeft((state, config)) {
          case ((currentState, conf), scalaVersion) =>
            val allModules: Set[ModuleID] = conf.getValidModule(scalaVersion) ++ libraries
            allModules.foldLeft((currentState, conf)) {
              case ((currentState, conf), module) =>
                executeCompilation(
                  state = currentState,
                  scalaVersion = scalaVersion,
                  config = conf,
                  m = module
                )
            }
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
      execute: ModuleID => SoteriaConfiguration
    ): Option[SoteriaConfiguration] = {
      executeDebugModuleCommand(log, module, execute)
    }
  }
}
