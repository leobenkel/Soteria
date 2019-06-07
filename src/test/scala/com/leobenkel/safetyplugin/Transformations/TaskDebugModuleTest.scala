package com.leobenkel.safetyplugin.Transformations

import com.leobenkel.safetyplugin.Modules.Dependency
import com.leobenkel.safetyplugin.ParentTest
import com.leobenkel.safetyplugin.Utils.LoggerExtended
import sbt.internal.util.complete.Parser
import sbt.util.Level

class TaskDebugModuleTest extends ParentTest with TaskDebugModule {
  private val test: TaskDebugModuleTest = this

  private abstract class LogTest(test: TaskDebugModuleTest) extends LoggerExtended {
    final override def isSoftError: Boolean = false

    final override def setSoftError(softError: Boolean): LoggerExtended = {
      test.fail("Should not be called")
    }

    final override def setLevel(level: Level.Value): LoggerExtended = {
      test.fail("Should not be called")
    }

    final override def separator(
      level: Level.Value,
      title: String
    ): Unit = {
      test.fail("Should not be called")
    }

    final override def trace(t: => Throwable): Unit = test.fail("Should not be called")

    final override def success(message: => String): Unit = test.fail("Should not be called")

    final override def log(
      level:   Level.Value,
      message: => String
    ): Unit = {
      test.fail("Should not be called")
    }
  }

  test("Test parser - fail all") {
    val p = ZTestOnlyTaskDebugModule.parser
    Seq(
      "",
      "input",
      """ "or """,
      """ "com.org" % " """,
      """ "com.org" % "arti """,
      """ "com.org" % "artifact" """,
      """ "com.org" % "artifact" % """,
      """ "com.org" % "artifact" % "version """,
      """ "com.org" % "artifact" %% "version" """,
      """ "com.org" %% " """,
      """ "com.org" %% "arti """,
      """ "com.org" %% "artifact" """,
      """ "com.org" %% "artifact" % """,
      """ "com.org" %% "artifact" % "version """,
      """ "com.org" %% "artifact" %% "version" """
    ).map(_.trim)
      .map(i => (i, Parser.parse(i, p)))
      .map(r => assert(r._2.isLeft, s"'${r._1}' should have failed, but got: ${r._2}"))
  }

  test("Test parser - succeed") {
    val p = ZTestOnlyTaskDebugModule.parser
    Seq(
      """ "com.org" % "artifact" % "version" """,
      """ "com.org" %% "artifact" % "version" """,
      """ "com.org" % "artifact" % "1.0" """,
      """ "com.org" %% "artifact" % "2.0" """
    ).map(_.trim)
      .map(i => (i, Parser.parse(i, p)))
      .map(r => assert(r._2.isRight, s"'${r._1}' should have succeed, but got: ${r._2}"))
  }

  test("Test command - fail no dependency") {
    val errorMessage = "Failed to get dependency"
    ZTestOnlyTaskDebugModule.execute(
      log = new LogTest(test) {
        override def criticalFailure(message: => String): Unit = {
          assertEquals(errorMessage, message)
        }
      },
      module = Left(errorMessage),
      execute = _ => test.fail("Should not be called")
    )
  }

  test("Test command - fail bad dependency 1") {
    val brokenDep = Dependency("com.org", "artifact")
    ZTestOnlyTaskDebugModule.execute(
      log = new LogTest(test) {
        override def criticalFailure(message: => String): Unit = {
          assert(message.contains(brokenDep.toString))
          assert(message.contains(brokenDep.toModuleID.left.get))

          ()
        }
      },
      module = Right(brokenDep),
      execute = _ => test.fail("Should not be called")
    )
  }

  test("Test command - fail bad dependency 2") {
    val brokenDep = Dependency("com.org", "artif-")
      .withVersion("1.0")
      .withName(_.copy(exactName = false))
    ZTestOnlyTaskDebugModule.execute(
      log = new LogTest(test) {
        override def criticalFailure(message: => String): Unit = {
          assert(message.contains(brokenDep.toString))
          assert(message.contains(brokenDep.toModuleID.left.get))

          ()
        }
      },
      module = Right(brokenDep),
      execute = _ => test.fail("Should not be called")
    )
  }

  test("Test command - good") {
    val dep = Dependency("com.org", "artif-").withVersion("1.0")
    ZTestOnlyTaskDebugModule.execute(
      log = new LogTest(test) {
        override def criticalFailure(message: => String): Unit = {
          test.fail("Should not be called")
        }
      },
      module = Right(dep),
      execute = m => {
        assertEquals(dep.organization, m.organization)
        assertEquals(dep.name, m.name)
        assertEquals(dep.version, Right(m.revision))

        ()
      }
    )
  }
}
