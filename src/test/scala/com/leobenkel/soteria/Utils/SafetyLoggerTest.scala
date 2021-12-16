package com.leobenkel.soteria.Utils

import com.leobenkel.soteria.ParentTest
import sbt.internal.util.ConsoleLogger
import sbt.util.Level

class SoteriaLoggerTest extends ParentTest {
  val Log: SoteriaLogger =
    SoteriaLogger(
      innerLog = ConsoleLogger(),
      level = Level.Debug,
      softError = true
    )

  test("Set Level") {
    assertEquals(Level.Debug, Log.TestOnly.getLevelTest)
    val newLog = Log.setLevel(Level.Info)
    assertEquals(Level.Debug, Log.TestOnly.getLevelTest)
    assertEquals(Level.Info, newLog.TestOnly.getLevelTest)
  }

  test("Test makeSeparator even") {
    val title = "Cool title"
    val separator = Log.TestOnly.makeSeparatorTest(title)
    assertEquals(SoteriaLogger.SeparatorLength, separator.length)
    assert(separator.contains(title))
  }

  test("Test makeSeparator odd") {
    val title = "Cool titles"
    val separator = Log.TestOnly.makeSeparatorTest(title)
    assertEquals(SoteriaLogger.SeparatorLength, separator.length)
    assert(separator.contains(title))
  }

  test("Test at level") {
    assert(Log.TestOnly.atLevelTest(Level.Info))
    assert(!Log.setLevel(Level.Error).TestOnly.atLevelTest(Level.Debug))
  }

  test("Test prepend header") {
    val message = "this is a message"
    assert(!message.contains(SoteriaLogger.Header))
    assert(
      Log.TestOnly.prependHeaderTest(message).contains(SoteriaLogger.Header)
    )
  }

  test("Test softError") {
    assert(Log.isSoftError)
    val softError = false
    val newLog = Log.setSoftError(softError)
    assert(Log.isSoftError)
    assert(!newLog.isSoftError)
  }
}
