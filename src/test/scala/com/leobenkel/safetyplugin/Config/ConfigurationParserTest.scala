package com.leobenkel.safetyplugin.Config

import com.leobenkel.safetyplugin.ParentTest
import com.leobenkel.safetyplugin.Utils.LoggerExtended
import sbt.util.Level

class ConfigurationParserTest extends ParentTest {
  val test: ConfigurationParserTest = this
  test("Test fail to parse json") {
    val badFileName = "badPath.txt"
    ConfigurationParser(
      log = new LogTest(test) {
        override def criticalFailure(message: => String): Unit = {
          test.assert(message.contains(badFileName))
          test.assert(message.contains(".json"))
        }
      },
      configPath = badFileName
    )
  }

  test("Test good json") {
    val goodFile = "goodFile.json"
    ConfigurationParser(
      log = new LogTest(test) {
        override def criticalFailure(message: => String): Unit = {
          test.fail("Should not be called")
        }
      },
      configPath = goodFile
    )
  }
}

abstract class LogTest(test: ConfigurationParserTest) extends LoggerExtended {
  override def isSoftError: Boolean = {
    test.fail("Should not be called")
    true
  }

  override def setSoftError(softError: Boolean): LoggerExtended = {
    test.fail("Should not be called")
    this
  }

  override def setLevel(level: Level.Value): LoggerExtended = {
    test.fail("Should not be called")
    this
  }

  override def separator(
    level: Level.Value,
    title: String
  ): Unit = {
    test.fail("Should not be called")
  }

  override def trace(t: => Throwable): Unit = test.fail("Should not be called")

  override def success(message: => String): Unit = test.fail("Should not be called")

  override def log(
    level:   Level.Value,
    message: => String
  ): Unit = {
    test.fail("Should not be called")
  }
}
