package com.leobenkel.soteria.Transformations

import com.leobenkel.soteria.Messages.NoError
import com.leobenkel.soteria.Modules.Dependency
import com.leobenkel.soteria.ParentTest
import com.leobenkel.soteria.Utils.LoggerExtended
import sbt.util.Level

// scalastyle:off magic.number
class TaskUpdateTest extends ParentTest with TaskUpdate {
  private val test: TaskUpdateTest = this

  abstract class LogTest() extends LoggerExtended {
    override def isSoftError: Boolean = test.fail("Should not be called")

    override def setSoftError(softError: Boolean): LoggerExtended = {
      test.fail("Should not be called")
    }

    override def criticalFailure(message: => String): Unit =
      test.fail("Should not be called")

    override def setLevel(level: Level.Value): LoggerExtended = {
      test.fail("Should not be called")
    }

    override def trace(t: => Throwable): Unit =
      test.fail("Should not be called")

    override def success(message: => String): Unit =
      test.fail("Should not be called")
  }

  test("test CombineModule") {
    val output = ZTestOnlyTaskUpdate.combineModulesTest(
      Seq(
        Dependency(
          "com.org",
          "artifact"
        ).withVersion("1.0"),
        Dependency(
          "com.org",
          "artifact2"
        ).withVersion("2.0"),
        Dependency(
          "com.org2",
          "artifact3"
        ).withVersion("3.0"),
        Dependency(
          "com.org",
          "artifact"
        ).withVersion("1.0"),
        Dependency(
          "com.org",
          "artifact2"
        ).withVersion("5.0")
      )
    )

    assertEquals(3, output.length)

    val versions1 =
      output
        .find(m => m.organization == "com.org" && m.name == "artifact")
        .map(_.versions)
    assert(versions1.isDefined)
    assert(versions1.get.size == 1)
    assert(versions1.get.contains("1.0"))

    val versions2 =
      output
        .find(m => m.organization == "com.org" && m.name == "artifact2")
        .map(_.versions)
    assert(versions2.isDefined)
    assert(versions2.get.contains("2.0"))
    assert(versions2.get.contains("5.0"))
  }

  test("test lastCheckUpTest - empty") {
    val (goodModules, errors) = ZTestOnlyTaskUpdate.checkTooManyVersionsTest(
      log = new LogTest() {
        override def separator(
            level: Level.Value,
            title: String
        ): Unit = {
          test.assert(title.contains("TaskUpdate"))
          test.assert(title.contains("checkTooManyVersions"))

          ()
        }

        override def log(
            level: Level.Value,
            message: => String
        ): Unit = {
          test.fail("Should not be called")
        }
      },
      loadedModules = Seq()
    )

    assertEquals(0, goodModules.length)
    assert(errors == NoError)
  }

  test("test lastCheckUpTest - not empty") {
    val badModule =
      Dependency("com.org", "artifact2").copy(versions = Set("v2.0", "3.0"))

    val (goodModules, errors) = ZTestOnlyTaskUpdate.checkTooManyVersionsTest(
      log = new LogTest() {
        override def separator(
            level: Level.Value,
            title: String
        ): Unit = {
          test.assert(title.contains("TaskUpdate"))
          test.assert(title.contains("checkTooManyVersions"))

          ()
        }

        override def log(
            level: Level.Value,
            message: => String
        ): Unit = {
          test.fail("Should not be called")
        }
      },
      loadedModules = Seq(
        Dependency("com.org", "artifact").withVersion("v1.0"),
        badModule
      )
    )

    assertEquals(1, goodModules.length)
    errors.consume { s =>
      assert(s.contains("1"))
      assert(s.contains(badModule.toString))

      ()
    }
  }

  test("test print debug") {
    val allModules1 = Seq(
      Dependency("com.orgs", "artifact"),
      Dependency("com.orgs", "artifact2")
    )
    val allModules2 = Seq(
      Dependency("com.orgs", "artifact"),
      Dependency("com.orgs", "artifact2"),
      Dependency("com.orgs", "artifact3")
    )

    val allCategories = Seq(
      "header1" -> allModules1,
      "header2" -> allModules2
    )

    var allMessage = ""

    ZTestOnlyTaskUpdate.printDebugTest(
      log = new LogTest() {
        override def separator(
            level: Level.Value,
            title: String
        ): Unit = {
          test.fail("Should not be called")
        }

        override def log(
            level: Level.Value,
            message: => String
        ): Unit = {
          allMessage += "\n" + message
        }
      },
      allModule = allCategories
    )

    allMessage.contains(allCategories.length.toString)
    allCategories
      .map(_._2.length.toString)
      .foreach(k => assert(allMessage.contains(k)))
    allCategories.map(_._1).foreach(k => assert(allMessage.contains(k)))
    allCategories
      .flatMap(_._2)
      .map(_.toString)
      .foreach(m => assert(allMessage.contains(m)))
  }
}
// scalastyle:on magic.number
