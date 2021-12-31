package com.leobenkel.soteria.Utils

import sbt.util.{Level, Logger}

private[soteria] trait LoggerExtended extends Logger {

  def isSoftError: Boolean

  def setSoftError(softError: Boolean): LoggerExtended

  def criticalFailure(message: => String): Unit

  def setLevel(level: Level.Value): LoggerExtended

  def separator(
    level: Level.Value,
    title: String
  ): Unit

  final def separatorInfo(title: String):  Unit = separator(Level.Info, title)
  final def separatorDebug(title: String): Unit = separator(Level.Debug, title)

  def fail(message: => String): Unit =
    if (isSoftError)
      error(message)
    else
      criticalFailure(message)

}
