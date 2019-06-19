package com.leobenkel.safetyplugin

import com.leobenkel.safetyplugin.Utils.LoggerExtended
import sbt.util.Level

class LogTest(test: ParentTest) extends LoggerExtended {
  override def isSoftError: Boolean = false

  override def setSoftError(softError: Boolean): LoggerExtended = {
    test.fail("Should not be called")
  }

  override def criticalFailure(message: => String): Unit = test.fail("Should not be called")

  override def setLevel(level: Level.Value): LoggerExtended = test.fail("Should not be called")

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
