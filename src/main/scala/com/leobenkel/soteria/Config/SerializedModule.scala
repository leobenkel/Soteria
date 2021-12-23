package com.leobenkel.soteria.Config

import com.leobenkel.soteria.Modules
import com.leobenkel.soteria.Modules._
import com.leobenkel.soteria.Utils.EitherUtils._
import com.leobenkel.soteria.Utils.Json.JsonDecode
import com.leobenkel.soteria.Utils.Json.JsonParserHelper._

/** Equivalent of [[Modules.Dependency]] for the json structure. */
case class SerializedModule(
  version:              String,
  exactName:            Option[Boolean],
  excludeName:          Option[Seq[String]],
  needDoublePercent:    Option[Boolean],
  shouldDownload:       Option[Boolean],
  overrideIsEnough:     Option[Boolean],
  forbidden:            Option[String],
  shouldBeProvided:     Option[Boolean],
  dependenciesToRemove: Option[Seq[String]],
  scalaVersionsFilter:  Option[Seq[String]]
) extends JsonDecode.Encoder {
  @transient lazy val versions: Set[String] =
    if (version.toLowerCase == SerializedModule.DefaultVersionString.toLowerCase)
      Set.empty
    else
      Set(version)

  def toNameOfModule(
    org:  String,
    name: String
  ): NameOfModule =
    NameOfModule(
      organization = org,
      name = name,
      exactName = this.exactName.getOrElse(ModuleDefaults.ExactName),
      excludeName = this.excludeName.getOrElse(Nil),
      needDoublePercent = this.needDoublePercent.getOrElse(ModuleDefaults.NeedDoublePercent)
    )

  def toDependency(
    org:       String,
    name:      String,
    retrieval: String => Either[String, NameOfModule]
  ): (Dependency, Seq[String]) = {
    val parsedDependencies =
      this.dependenciesToRemove.getOrElse(Nil).map(retrieval)
    val parsedScalaVersion =
      this.scalaVersionsFilter.getOrElse(Nil).map(ScalaVersionHandler(_))
    val errors = parsedDependencies.flattenLeft ++ parsedScalaVersion.flattenLeft

    (
      Modules.Dependency(
        nameObj = this.toNameOfModule(org, name),
        versions = this.versions,
        shouldDownload = this.shouldDownload.getOrElse(ModuleDefaults.ShouldDownload),
        shouldBeProvided = this.shouldBeProvided.getOrElse(ModuleDefaults.ShouldBeProvided),
        overrideIsEnough = this.overrideIsEnough.getOrElse(ModuleDefaults.OverrideIsEnough),
        forbidden = this.forbidden,
        dependenciesToRemove = parsedDependencies.flattenEI,
        scalaVersionsFilter = parsedScalaVersion.flattenEI
      ),
      errors
    )
  }

  lazy override protected val asMap: Map[String, Any] =
    Map[String, Any](
      "version"              -> this.version,
      "exactName"            -> this.exactName,
      "excludeName"          -> this.excludeName.map(_.toList.sorted),
      "needDoublePercent"    -> this.needDoublePercent,
      "shouldDownload"       -> this.shouldDownload,
      "overrideIsEnough"     -> this.overrideIsEnough,
      "forbidden"            -> this.forbidden,
      "shouldBeProvided"     -> this.shouldBeProvided,
      "dependenciesToRemove" -> this.dependenciesToRemove.map(_.toList.sorted),
      "scalaVersionsFilter"  -> this.scalaVersionsFilter.map(_.toList.sorted)
    )
}

object SerializedModule {
  val DefaultVersionString: String = "None"
  val Empty: SerializedModule = SerializedModule(
    version = "",
    exactName = None,
    excludeName = None,
    needDoublePercent = None,
    shouldBeProvided = None,
    shouldDownload = None,
    overrideIsEnough = None,
    forbidden = None,
    dependenciesToRemove = None,
    scalaVersionsFilter = None
  )

  implicit val parser: (String, String) => JsonDecode.Parser[SerializedModule] =
    (org: String, name: String) => { input: Map[String, Any] =>
      {
        (for {
          version           <- input.getAs[String]("version")
          exactName         <- input.getOption[Boolean]("exactName")
          excludeName       <- input.getOption[List[String]]("excludeName")
          needDoublePercent <- input.getOption[Boolean]("needDoublePercent")
          shouldDownload    <- input.getOption[Boolean]("shouldDownload")
          overrideIsEnough  <- input.getOption[Boolean]("overrideIsEnough")
          forbidden         <- input.getOption[String]("forbidden")
          shouldBeProvided  <- input.getOption[Boolean]("shouldBeProvided")
          dependenciesToRemove <- input.getOption[List[String]](
            "dependenciesToRemove"
          )
          scalaVersionsFilter <- input.getOption[List[String]](
            "scalaVersionsFilter"
          )
        } yield SerializedModule(
          version,
          exactName,
          excludeName.map(_.toSeq),
          needDoublePercent,
          shouldDownload,
          overrideIsEnough,
          forbidden,
          shouldBeProvided,
          dependenciesToRemove.map(_.toSeq),
          scalaVersionsFilter.map(_.toSeq)
        )).left.map(s => s"$s |in: org: '$org', name: '$name'")
      }
    }
}
