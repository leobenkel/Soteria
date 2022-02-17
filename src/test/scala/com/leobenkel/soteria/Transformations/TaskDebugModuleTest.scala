package com.leobenkel.soteria.Transformations

import com.leobenkel.soteria.{LogTest, ParentTest}
import com.leobenkel.soteria.Config.SoteriaConfiguration
import com.leobenkel.soteria.Modules.Dependency
import sbt.internal.util.complete.Parser

class TaskDebugModuleTest extends ParentTest with TaskDebugModule {
  private val test: TaskDebugModuleTest = this

  test("Test parser - fail all") {
    val p   = ZTestOnlyTaskDebugModule.parser
    val bad =
      Seq(
        "", "input", """ "or """, """ "com.org" % " """, """ "com.org" % "arti """,
        """ "com.org" % "artifact" """, """ "com.org" % "artifact" % """,
        """ "com.org" % "artifact" % "version """, """ "com.org" % "artifact" %% "version" """,
        """ "com.org % "artifact % "version """, """ com.org" % artifact" % version" """,
        """ "com.org" %% " """, """ "com.org" %% "arti """, """ "com.org" %% "artifact" """,
        """ "com.org" %% "artifact" % """, """ "com.org" %% "artifact" % "version """,
        """ "com.org %% "artifact % "version """, """ com.org" %% artifact" % version" """,
        """ "com.org" %% "artifact" %% "version" """,
      )

    (bad.map(_.trim) ++ bad)
      .map(i => (i, Parser.parse(i, p)))
      .map(r => assert(r._2.isLeft, s"'${r._1}' should have failed, but got: ${r._2}"))
  }

  test("Test parser - succeed") {
    val p    = ZTestOnlyTaskDebugModule.parser
    val good =
      Seq(
        """ "com.org" % "artifact" % "version" """, """ "com.org" %% "artifact" % "version" """,
        """ "com.org" % "artifact" % "1.0" """, """ "com.org" %% "artifact" % "2.0" """,
        """ com.org % artifact % version """, """ com.org %% artifact % version """,
        """ com.org % artifact % 1.0 """, """ com.org %% artifact % 2.0 """,
      )

    (good.map(_.trim) ++ good)
      .map(i => (i, Parser.parse(i, p)))
      .map(r =>
        assert(
          r._2.isRight,
          s"'${r._1}' should have succeed, but got: ${r._2}",
        )
      )
  }

  test("Test command - fail no dependency") {
    val errorMessage = "Failed to get dependency"
    ZTestOnlyTaskDebugModule.execute(
      log =
        new LogTest(test) {
          override def criticalFailure(message: => String): Unit =
            assertEquals(errorMessage, message)
        },
      module = Left(errorMessage),
      execute = _ => test.fail("Should not be called"),
    )
  }

  test("Test command - fail bad dependency 1") {
    val brokenDep = Dependency("com.org", "artifact")
    ZTestOnlyTaskDebugModule.execute(
      log =
        new LogTest(test) {
          override def criticalFailure(message: => String): Unit = {
            assert(message.contains(brokenDep.toString))
            assert(message.contains(brokenDep.toModuleID.left.get))

            ()
          }
        },
      module = Right(brokenDep),
      execute = _ => test.fail("Should not be called"),
    )
  }

  test("Test command - fail bad dependency 2") {
    val brokenDep =
      Dependency("com.org", "artif-").withVersion("1.0").withName(_.copy(exactName = false))
    ZTestOnlyTaskDebugModule.execute(
      log =
        new LogTest(test) {
          override def criticalFailure(message: => String): Unit = {
            assert(message.contains(brokenDep.toString))
            assert(message.contains(brokenDep.toModuleID.left.get))

            ()
          }
        },
      module = Right(brokenDep),
      execute = _ => test.fail("Should not be called"),
    )
  }

  test("Test command - good") {
    val dep = Dependency("com.org", "artif-").withVersion("1.0")
    val log =
      new LogTest(test) {
        override def criticalFailure(message: => String): Unit = test.fail("Should not be called")
      }

    ZTestOnlyTaskDebugModule.execute(
      log = log,
      module = Right(dep),
      execute =
        m => {
          assertEquals(dep.organization, m.organization)
          assertEquals(dep.name, m.name)
          assertEquals(dep.version, Right(m.revision))

          SoteriaConfiguration(log, "", Set.empty, Seq.empty, Map.empty, None)
        },
    )
  }
}
