package com.leobenkel.safetyplugin.Utils

import sbt.util.{Level, Logger}

private[safetyplugin] trait LoggerExtended extends Logger {
  def fail(message: String): Unit

  def isSoftError: Boolean

  def setSoftError(softError: Boolean): LoggerExtended

  def separatorInfo(title: String): Unit

  def separatorDebug(title: String): Unit

  def separator(
    level: Level.Value,
    title: String
  ): Unit

  def setLevel(level: Level.Value): LoggerExtended
}
