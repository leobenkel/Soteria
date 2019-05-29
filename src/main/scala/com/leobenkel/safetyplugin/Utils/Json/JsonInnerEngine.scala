package com.leobenkel.safetyplugin.Utils.Json

import scala.util.{Either, Left, Right, Try}
import scala.util.parsing.json._

/**
  * Have to use native scala deprecated parser because no other libary worked.
  * Open question:
  * * https://stackoverflow.com/questions/55895632/how-can-i-add-a-dependency-to-my-sbt-plugin
  * * https://github.com/circe/circe/issues/823#issuecomment-487418960
  */
private[Json] object JsonInnerEngine {
  def parse[A](
    input:  String,
    parser: JsonDecode.Parser[A]
  ): Either[String, A] = {
    Try(
      JSON
        .parseFull(input)
        .map(_.asInstanceOf[Map[String, Any]])
    ).toEither.left
      .map(_.toString)
      .right.flatMap {
        case None => Left("Did not parse")
        case Some(v) =>
          parser(v) match {
            case Right(vv) => Right(vv)
            case Left(ex)  => Left(s"The parser failed: $ex")
          }
      }
  }
}
