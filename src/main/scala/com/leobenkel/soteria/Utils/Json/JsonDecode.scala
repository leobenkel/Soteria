package com.leobenkel.soteria.Utils.Json

import com.leobenkel.soteria.Utils.Json.FilterNulls._

import scala.util._

private[soteria] object JsonDecode {
  type Parser[A] = Map[String, Any] => Either[String, A]

  trait Encoder {
    protected def asMap: Map[String, Any]

    lazy final val toJsonStructure: Either[String, Map[String, Any]] = {
      Try(asMap.filterNullsOut).toEither.left.map(_.toString)
    }
  }

  def parse[A](json: String)(implicit parser: Parser[A]): Either[String, A] = {
    JsonInnerEngine.parse(json, parser)
  }

  def encode[A <: Encoder](input: A): Either[String, String] = {
    JsonInnerEngine.encode(input)
  }
}
