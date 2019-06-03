package com.leobenkel.safetyplugin.Transformations

import com.leobenkel.safetyplugin.Modules.Dependency
import com.leobenkel.safetyplugin.SafetyPluginKeys
import sbt.{Command, Keys, Project, Test}
import sbt.internal.util.complete.Parser

private[Transformations] trait TaskDebugModule {

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
        val log = Project.extract(state).get(SafetyPluginKeys.safetyGetLog)

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
                      SafetyPluginKeys.safetyDebugModule         := Some(m.organization, m.name),
                      SafetyPluginKeys.safetyDebugPrintScalaCode := true
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
}
