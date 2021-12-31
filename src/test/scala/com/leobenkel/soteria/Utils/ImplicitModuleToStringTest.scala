package com.leobenkel.soteria.Utils

import com.leobenkel.soteria.Modules.Dependency
import com.leobenkel.soteria.ParentTest
import sbt._
import sbt.util.Level

class ImplicitModuleToStringTest extends ParentTest {
  import ImplicitModuleToString._

  private val test: ImplicitModuleToStringTest = this

  test("Test ModuleID to prettyString") {
    val org = "com.org"
    val name = "artifact-name"
    val revision = "0.1"
    val m: ModuleID = org % name % revision

    val prettyString = m.prettyString

    assert(prettyString.contains(org))
    assert(prettyString.contains(name))
    assert(prettyString.contains(revision))
    assert(prettyString.contains("%"))
  }

  test("Test Seq[ModuleID] to prettyString") {
    val org = "com.org"
    val name = "artifact-name"
    val revisions = (1 until 5).map(v => s"0.$v")
    val m: Seq[ModuleID] = revisions.map(r => org % name % r)
    val header = "this is a header"
    m.prettyString(
      new LoggerExtended() {
        override def isSoftError: Boolean = test.fail("Should not be called")

        override def setSoftError(softError: Boolean): LoggerExtended =
          test.fail("Should not be called")

        override def separator(
          level: Level.Value,
          title: String
        ): Unit = test.fail("Should not be called")

        override def setLevel(level: Level.Value): LoggerExtended =
          test.fail("Should not be called")

        override def trace(t: => Throwable): Unit = test.fail("Should not be called")

        override def success(message: => String): Unit = test.fail("Should not be called")

        override def log(
          level:        Level.Value,
          prettyString: => String
        ): Unit = {
          assertEquals(Level.Debug, level)
          assert(prettyString.contains(header))
          assert(prettyString.contains(org))
          assert(prettyString.contains(name))
          assert(revisions.exists(prettyString.contains))

          ()
        }

        override def criticalFailure(message: => String): Unit = test.fail("Should not be called")
      },
      header
    )
  }

  test("Test Seq[Dependency] to prettyString") {
    val org = "com.org"
    val name = "artifact-name"
    val revisions = (1 until 5).map(v => s"0.$v")
    val m: Seq[Dependency] = revisions.map(r => Dependency(org % name % r))
    val header = "this is a header"
    m.prettyString(
      new LoggerExtended() {
        override def isSoftError: Boolean = test.fail("Should not be called")

        override def setSoftError(softError: Boolean): LoggerExtended =
          test.fail("Should not be called")

        override def separator(
          level: Level.Value,
          title: String
        ): Unit = test.fail("Should not be called")

        override def setLevel(level: Level.Value): LoggerExtended =
          test.fail("Should not be called")

        override def trace(t: => Throwable): Unit = test.fail("Should not be called")

        override def success(message: => String): Unit = test.fail("Should not be called")

        override def log(
          level:        Level.Value,
          prettyString: => String
        ): Unit = {
          assertEquals(Level.Debug, level)
          assert(prettyString.contains(header))
          assert(prettyString.contains(org))
          assert(prettyString.contains(name))
          assert(revisions.exists(prettyString.contains))

          ()
        }

        override def criticalFailure(message: => String): Unit = test.fail("Should not be called")
      },
      header
    )
  }
}
