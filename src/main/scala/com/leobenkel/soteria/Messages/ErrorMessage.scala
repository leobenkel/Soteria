package com.leobenkel.soteria.Messages

import com.leobenkel.soteria.Messages.CommonMessage._

sealed trait ErrorMessage {
  def consume(function: String => Unit): Unit

  def ++(other: ErrorMessage): Errors

  def toErrors: Errors
}

case class Errors(errorMessage: Seq[WithErrorMessage]) {
  def consume(logFail: String => Unit): Unit =
    if (errorMessage.nonEmpty)
      logFail(this.toString)
    else
      ()

  override def toString: String =
    if (errorMessage.nonEmpty)
      errorMessage.map(_.toString).toError("Found blocks of errors").toString
    else
      "No Error"

  def ++(other: Errors): Errors =
    this.copy(
      errorMessage = this.errorMessage ++ other.errorMessage
    )

  def resolve(successMessage: SuccessMessage): CommonMessage.ResultMessages =
    if (errorMessage.isEmpty)
      Right(successMessage)
    else
      Left(this)

  def resolve(successMessage: String): CommonMessage.ResultMessages =
    resolve(SuccessMessage(successMessage))
}

case class WithErrorMessage(
  title:    String,
  messages: Seq[String]
) extends ErrorMessage {
  override def ++(other: ErrorMessage): Errors =
    other match {
      case NoError => Errors(Seq(this))
      case error: WithErrorMessage => Errors(Seq(this, error))
    }

  override def toErrors: Errors = Errors(Seq(this))

  private def getAllLines: Seq[String] = messages.flatMap(_.split("\n"))

  override def toString: String =
    if (messages.isEmpty)
      title
    else
      s"$title (${messages.size}) :\n${getAllLines.map(m => s"  $m").mkString("\n")}"

  override def consume(log: String => Unit): Unit = log(this.toString)
}

case object NoError extends ErrorMessage {
  override def ++(other: ErrorMessage): Errors =
    other match {
      case NoError => Errors(Seq.empty)
      case error: WithErrorMessage => Errors(Seq(error))
    }

  override def toErrors: Errors = Errors(Seq.empty)

  override def consume(noLog: String => Unit): Unit = ()
}

object ErrorMessage {
  val Empty: ErrorMessage = NoError

  def apply(
    title:   String,
    message: String
  ): WithErrorMessage = WithErrorMessage(title, Seq(message))

  def apply(
    title:    String,
    messages: Seq[String]
  ): WithErrorMessage = WithErrorMessage(title, messages)
}
