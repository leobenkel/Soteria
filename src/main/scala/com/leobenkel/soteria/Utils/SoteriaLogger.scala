package com.leobenkel.soteria.Utils

import sbt.internal.util.ConsoleLogger
import sbt.util.Level

case class SoteriaLogger(
  innerLog:  ConsoleLogger,
  level:     Level.Value,
  softError: Boolean
) extends sbt.util.Logger with LoggerExtended {

  /**
    * For test only
    */
  object TestOnly {
    @inline def makeSeparatorTest(title: String): String = makeSeparator(title)
    @transient lazy val getLevelTest: Level.Value = getLevel
    @inline def atLevelTest(level:         Level.Value): Boolean = atLevel(level)
    @inline def prependHeaderTest(message: => String):   String = prependHeader(message)
  }

  innerLog.setLevel(Level.Debug)

  @transient lazy private val getLevel: Level.Value = this.level

  override def setLevel(level: Level.Value): SoteriaLogger = this.copy(level = level)

  private def atLevel(level: Level.Value): Boolean = level.id >= getLevel.id

  private def prependHeader(message: => String): String = s"[${SoteriaLogger.Header}] $message"

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

  override def fail(message: => String): Unit = super.fail(prependHeader(message))

  override def criticalFailure(message: => String): Unit = sys.error(prependHeader(message))

  override def separator(
    level: Level.Value,
    title: String
  ): Unit = {
    log(level, makeSeparator(title))
  }

  private def makeSeparator(title: String): String = {
    val goodTitle = title.trim
    val LeftForEdge = Math.ceil((SoteriaLogger.SeparatorLength - goodTitle.length - 4) / 2).toInt
    val edge = SoteriaLogger.SeparatorCharacter * LeftForEdge
    val fullMessage = s"$edge< $goodTitle >$edge"
    val extra = if (fullMessage.length < SoteriaLogger.SeparatorLength) {
      SoteriaLogger.SeparatorCharacter
    } else {
      ""
    }
    fullMessage + extra
  }

  override def isSoftError: Boolean = this.softError

  override def setSoftError(softError: Boolean): SoteriaLogger = this.copy(softError = softError)
}

object SoteriaLogger {
  val Header:             String = "Soteria"
  val SeparatorLength:    Int = 70
  val SeparatorCharacter: String = "-"
}
