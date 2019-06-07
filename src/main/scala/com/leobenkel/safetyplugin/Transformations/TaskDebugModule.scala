package com.leobenkel.safetyplugin.Transformations

import com.leobenkel.safetyplugin.Modules.Dependency
import com.leobenkel.safetyplugin.SafetyPluginKeys
import com.leobenkel.safetyplugin.Utils.LoggerExtended
import sbt.internal.util.complete.Parser
import sbt.librarymanagement.ModuleID
import sbt.{Command, Keys, Project, State, Test}
import xsbti.compile.CompileAnalysis

private[Transformations] trait TaskDebugModule {
  private val CommandName: String = "safetyDebugModuleWithCode"

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

  private def executeDebugModuleCommand(
    log:     LoggerExtended,
    module:  Either[String, Dependency],
    execute: ModuleID => Unit
  ): Unit = {
    module match {
      case Right(module: Dependency) =>
        val moduleId = module.toModuleID

        moduleId match {
          case Right(m) => execute(m)
          case Left(e)  => log.fail(s"Module '$module' is invalid: $e")
        }
      case Left(e) => log.fail(e)
    }
  }

  def executeCompilation(
    state: State,
    m:     ModuleID
  ): (State, CompileAnalysis) = {
    val newState = Project
      .extract(state).appendWithoutSession(
        Seq(
          Keys.libraryDependencies += m,
          SafetyPluginKeys.safetyDebugModule         := Some(m.organization, m.name),
          SafetyPluginKeys.safetyDebugPrintScalaCode := true
        ),
        state
      )

    Project.extract(newState).runTask(Test / Keys.compile, newState)
  }

  def debugModuleCommand: Command = {
    Command
      .args(CommandName, "") { (state, args) =>
        val result: Either[String, Dependency] = Parser.parse(args.mkString(" ").trim, parseModule)
        val log = Project.extract(state).get(SafetyPluginKeys.safetyGetLog)

        executeDebugModuleCommand(
          log = log,
          module = result,
          execute = m => {
            executeCompilation(state, m)
            ()
          }
        )

        state
      }
  }

  object ZTestOnlyTaskDebugModule {
    val parser = parseModule

    def execute(
      log:     LoggerExtended,
      module:  Either[String, Dependency],
      execute: ModuleID => Unit
    ): Unit = {
      executeDebugModuleCommand(log, module, execute)
    }
  }
}
