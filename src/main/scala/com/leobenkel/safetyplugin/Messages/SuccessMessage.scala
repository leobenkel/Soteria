package com.leobenkel.safetyplugin.Messages

case class SuccessMessage(message: String) {
  def consume(log: String => Unit): Unit = log(message)
}
