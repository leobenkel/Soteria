package com.leobenkel.soteria.Config

import com.leobenkel.soteria.ParentTest
import com.leobenkel.soteria.Utils.LoggerExtended
import sbt.util.Level

class ConfigurationParserTest extends ParentTest {
  private val test: ConfigurationParserTest = this

  private abstract class LogTest(test: ConfigurationParserTest) extends LoggerExtended {
    override def isSoftError: Boolean = test.fail("Should not be called")

    override def setSoftError(softError: Boolean): LoggerExtended =
      test.fail("Should not be called")

    override def setLevel(level: Level.Value): LoggerExtended = test.fail("Should not be called")

    override def separator(
      level: Level.Value,
      title: String
    ): Unit = test.fail("Should not be called")

    override def trace(t: => Throwable): Unit = test.fail("Should not be called")

    override def success(message: => String): Unit = test.fail("Should not be called")

    override def log(
      level:   Level.Value,
      message: => String
    ): Unit = test.fail("Should not be called")
  }

  test("Test fail to parse json") {
    val badFileName = "badPath.txt"
    ConfigurationParser(
      log = new LogTest(test) {
        override def criticalFailure(message: => String): Unit = {
          test.assert(message.contains(badFileName))
          test.assert(message.contains(".json"))
          ()
        }
      },
      configPath = badFileName
    )
  }

  test("Test good json") {
    val goodFile = "goodFile.json"
    ConfigurationParser(
      log = new LogTest(test) {
        override def criticalFailure(message: => String): Unit = test.fail("Should not be called")
      },
      configPath = goodFile
    )
  }
}
