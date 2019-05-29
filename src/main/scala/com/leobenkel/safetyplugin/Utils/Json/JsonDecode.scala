package com.leobenkel.safetyplugin.Utils.Json

import scala.util._

private[safetyplugin] object JsonDecode {
  type Parser[A] = Map[String, Any] => Either[String, A]

  def parse[A](json: String)(implicit parser: Parser[A]): Either[String, A] = {
    JsonInnerEngine.parse(json, parser)
  }
}
