package com.leobenkel.safetyplugin.Messages

object CommonMessage {
  type ResultMessages = Either[Errors, SuccessMessage]

  implicit class StringsToError(messages: Seq[String]) {
    def toError(title: String): ErrorMessage = {
      if (messages.isEmpty) {
        NoError
      } else {
        WithErrorMessage(
          title = title,
          messages = messages
        )
      }
    }
  }

  implicit class StringToError(message: String) {
    def asError: WithErrorMessage = {
      ErrorMessage(title = message, messages = Seq.empty)
    }

    def asErrors: Errors = {
      message.asError.toErrors
    }
  }

}
