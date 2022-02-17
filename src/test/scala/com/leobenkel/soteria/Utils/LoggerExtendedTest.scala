package com.leobenkel.soteria.Utils

import com.leobenkel.soteria.ParentTest
import sbt.util.Level

class LoggerExtendedTest extends ParentTest {
  private val test: LoggerExtendedTest = this

  private abstract class LogSeparatorTest(test: LoggerExtendedTest) extends LoggerExtended {
    final override def isSoftError: Boolean = test.fail("Should not be called")

    final override def setSoftError(softError: Boolean): LoggerExtended =
      test.fail("Should not be called")

    final override def criticalFailure(message: => String): Unit = test.fail("Should not be called")

    final override def setLevel(level: Level.Value): LoggerExtended =
      test.fail("Should not be called")

    final override def trace(t: => Throwable): Unit = test.fail("Should not be called")

    final override def success(message: => String): Unit = test.fail("Should not be called")

    final override def log(
        level:   Level.Value,
        message: => String,
    ): Unit = test.fail("Should not be called")
  }

  private abstract class LogFailTest(test: LoggerExtendedTest) extends LoggerExtended {
    final override def setSoftError(softError: Boolean): LoggerExtended =
      test.fail("should not be called")

    final override def setLevel(level: Level.Value): LoggerExtended =
      test.fail("should not be called")

    final override def separator(
        level: Level.Value,
        title: String,
    ): Unit = test.fail("should not be called")

    final override def trace(t: => Throwable): Unit = test.fail("should not be called")

    final override def success(message: => String): Unit = test.fail("should not be called")
  }

  test("Test separator") {
    val titleInput = "Cool title"
    val log        =
      new LogSeparatorTest(test) {
        override def separator(
            level: Level.Value,
            title: String,
        ): Unit = assertEquals(titleInput, title)
      }

    log.separatorDebug(titleInput)
    log.separatorInfo(titleInput)
  }

  test("Test separator Info") {
    val titleInput = "Cool title"
    val log        =
      new LogSeparatorTest(test) {
        override def separator(
            level: Level.Value,
            title: String,
        ): Unit = {
          assertEquals(titleInput, title)
          assertEquals(Level.Info, level)
        }
      }

    log.separatorInfo(titleInput)
  }

  test("Test separator Debug") {
    val titleInput = "Cool title"
    val log        =
      new LogSeparatorTest(test) {
        override def separator(
            level: Level.Value,
            title: String,
        ): Unit = {
          assertEquals(titleInput, title)
          assertEquals(Level.Debug, level)
        }
      }

    log.separatorDebug(titleInput)
  }

  test("Test fail - soft") {
    val messageInput = "message"
    val log          =
      new LogFailTest(test) {
        override def isSoftError: Boolean = true

        override def criticalFailure(message: => String): Unit = test.fail("Should not be called")

        override def log(
            level:   Level.Value,
            message: => String,
        ): Unit = {
          assertEquals(messageInput, message)
          assertEquals(Level.Error, level)
        }
      }

    log.fail(messageInput)
  }

  test("Test fail - hard") {
    val messageInput = "message"
    val logTest      =
      new LogFailTest(test) {
        override def isSoftError: Boolean = false

        override def criticalFailure(message: => String): Unit = assertEquals(messageInput, message)

        override def log(
            level:   Level.Value,
            message: => String,
        ): Unit = test.fail("Should not be called")
      }

    logTest.fail(messageInput)
  }
}
