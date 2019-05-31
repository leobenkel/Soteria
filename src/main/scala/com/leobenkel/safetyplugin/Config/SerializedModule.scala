package com.leobenkel.safetyplugin.Config

import com.leobenkel.safetyplugin.Modules
import com.leobenkel.safetyplugin.Modules.{Dependency, ModuleDefaults, NameOfModule}
import com.leobenkel.safetyplugin.Utils.Json.JsonDecode
import com.leobenkel.safetyplugin.Utils.Json.JsonParserHelper._

/**
  * Equivalent of [[Modules.Dependency]] for the json structure.
  */
case class SerializedModule(
  version:              String,
  exactName:            Option[Boolean],
  excludeName:          Option[Array[String]],
  needDoublePercent:    Option[Boolean],
  shouldDownload:       Option[Boolean],
  overrideIsEnough:     Option[Boolean],
  forbidden:            Option[String],
  shouldBeProvided:     Option[Boolean],
  dependenciesToRemove: Option[Array[String]]
) {
  @transient lazy val versions: Set[String] =
    if (version == SerializedModule.DefaultVersionString) {
      Set.empty
    } else {
      Set(version)
    }

  def toNameOfModule(
    org:  String,
    name: String
  ): NameOfModule = {
    NameOfModule(
      organization = org,
      name = name,
      exactName = this.exactName.getOrElse(ModuleDefaults.ExactName),
      excludeName = this.excludeName.getOrElse(Array.empty).toSeq,
      needDoublePercent = this.needDoublePercent.getOrElse(ModuleDefaults.NeedDoublePercent)
    )
  }

  def toDependency(
    org:       String,
    name:      String,
    retrieval: String => Either[String, NameOfModule]
  ): (Dependency, Seq[String]) = {
    val parsed = this.dependenciesToRemove.getOrElse(Array.empty).map(retrieval)
    val errors = parsed.filter(_.isLeft).map(_.left.get)

    (
      Modules.Dependency(
        nameObj = this.toNameOfModule(org, name),
        versions = this.versions,
        shouldDownload = this.shouldDownload.getOrElse(ModuleDefaults.ShouldDownload),
        shouldBeProvided = this.shouldBeProvided.getOrElse(ModuleDefaults.ShouldBeProvided),
        overrideIsEnough = this.overrideIsEnough.getOrElse(ModuleDefaults.OverrideIsEnough),
        forbidden = this.forbidden,
        dependenciesToRemove = parsed.filter(_.isRight).map(_.right.get)
      ),
      errors
    )
  }
}

object SerializedModule {
  val DefaultVersionString: String = "None"

  implicit val parser: (String, String) => JsonDecode.Parser[SerializedModule] =
    (org: String, name: String) => {
      input: Map[String, Any] => {
        (for {
          version              <- input.getAs[String]("version")
          exactName            <- input.getOption[Boolean]("exactName")
          excludeName          <- input.getOption[List[String]]("excludeName")
          needDoublePercent    <- input.getOption[Boolean]("needDoublePercent")
          shouldDownload       <- input.getOption[Boolean]("shouldDownload")
          overrideIsEnough     <- input.getOption[Boolean]("overrideIsEnough")
          forbidden            <- input.getOption[String]("forbidden")
          shouldBeProvided     <- input.getOption[Boolean]("shouldBeProvided")
          dependenciesToRemove <- input.getOption[List[String]]("dependenciesToRemove")
        } yield {
          SerializedModule(
            version,
            exactName,
            excludeName.map(_.toArray),
            needDoublePercent,
            shouldDownload,
            overrideIsEnough,
            forbidden,
            shouldBeProvided,
            dependenciesToRemove.map(_.toArray)
          )
        }).left.map(s => s"$s |in: org: '$org', name: '$name'")
      }
    }
}
