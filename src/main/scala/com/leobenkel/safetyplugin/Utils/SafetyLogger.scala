package com.leobenkel.safetyplugin.Utils

import sbt.internal.util.ConsoleLogger
import sbt.util.Level

case class SafetyLogger(
  innerLog:  ConsoleLogger,
  level:     Level.Value,
  softError: Boolean
) extends sbt.util.Logger with LoggerExtended {
  innerLog.setLevel(Level.Debug)

  def getLevel: Level.Value = this.level

  def setLevel(level: Level.Value): SafetyLogger = this.copy(level = level)

  def atLevel(level: Level.Value): Boolean = level.id >= getLevel.id

  private def prependHeader(message: => String): String = s"[SafetyPlugin] $message"

  override def trace(t: => Throwable): Unit = innerLog.trace(t)

  override def success(message: => String): Unit = innerLog.success(message)

  override def log(
    level:   Level.Value,
    message: => String
  ): Unit = {
    if (atLevel(level)) {
      innerLog.log(level, prependHeader(message))
    }
  }

  def fail(message: String): Unit = {
    if (softError) {
      error(prependHeader(message))
    } else {
      criticalFailure(message)
    }
  }

  def criticalFailure(message: String): Unit = {
    sys.error(prependHeader(message))
  }

  override def separatorInfo(title: String): Unit = separator(Level.Info, title)

  override def separatorDebug(title: String): Unit = separator(Level.Debug, title)

  override def separator(
    level: Level.Value,
    title: String
  ): Unit = {
    val totalLineLength = 70
    val LeftForEdge = Math.ceil((totalLineLength - title.length - 4) / 2).toInt
    val edge = (0 until LeftForEdge).map(_ => "-").mkString("")
    val fullMessage = s"$edge< $title >$edge"
    val extra = if (fullMessage.length < totalLineLength) {
      "-"
    } else {
      ""
    }
    log(level, fullMessage + extra)
  }

  override def isSoftError: Boolean = this.softError

  override def setSoftError(softError: Boolean): SafetyLogger = this.copy(softError = softError)
}
