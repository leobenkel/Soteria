package com.leobenkel.soteria.Utils.Json

import com.github.ghik.silencer.silent
import com.leobenkel.soteria.Utils.Json.FilterNulls._
import com.leobenkel.soteria.Utils.Json.JsonDecode.Encoder
import scala.util.{Either, Left, Right, Try}
import scala.util.parsing.json._

/**
 * Have to use native scala deprecated parser because no other library worked. Open question: *
 * https://stackoverflow.com/questions/55895632/how-can-i-add-a-dependency-to-my-sbt-plugin *
 * https://github.com/circe/circe/issues/823#issuecomment-487418960
 * @deprecated
 *   "This should be replaced by a better json library like Circe"
 */
@silent("deprecated")
private[Json] object JsonInnerEngine {
  def parse[A](
    input:  String,
    parser: JsonDecode.Parser[A]
  ): Either[String, A] =
    Try(
      JSON.parseFull(input).map(_.asInstanceOf[Map[String, Any]])
    ).toEither
      .left
      .map(_.toString)
      .right
      .flatMap {
        case None => Left("Did not parse")
        case Some(v) =>
          parser(v) match {
            case Right(vv) => Right(vv)
            case Left(ex)  => Left(s"The parser failed: $ex")
          }
      }

  private def unsafeConvert[A <: Encoder](input: A): Map[String, Any] =
    input.toJsonStructure.right.get

  private def convert(input: Map[_, _]): Map[String, Any] =
    input
      .filterNullsOut
      .mapValues {
        case v: JsonDecode.Encoder => JSONObject(convert(unsafeConvert(v)))
        case v: Map[_, _]          => JSONObject(convert(v))
        case v: List[_]            => JSONArray(v)
        case v => v
      }

  def encode[A <: Encoder](input: A): Either[String, String] =
    input
      .toJsonStructure
      .right
      .flatMap(inputMap =>
        Try(
          JSONObject.apply(convert(inputMap)).toString(JSONFormat.defaultFormatter)
        ).toEither.left.map(_.toString)
      )
}
