package com.leobenkel.safetyplugin.Config

import com.leobenkel.safetyplugin.Modules.{Dependency, NameOfModule}
import com.leobenkel.safetyplugin.Utils.Json.JsonDecode
import com.leobenkel.safetyplugin.Utils.Json.JsonParserHelper._
import sbt.librarymanagement.ModuleID
import com.leobenkel.safetyplugin.Utils.EitherUtils._

/**
  * What is read from the JSON config file
  */
private[safetyplugin] case class SafetyConfiguration(
  sbtVersion:     String,
  scalaVersions:  Set[String],
  scalaCFlags:    Array[String],
  modules:        Map[String, Map[String, SerializedModule]],
  dockerImageOpt: Option[String]
) {
  @transient lazy private val retrieval: String => Either[String, NameOfModule] =
    NameOfModule.find(modules)
  @transient lazy val AllModules: Seq[Dependency] = modules.flatMap {
    case (org, mm) =>
      mm.map {
        case (name, m) =>
          val (modules, errors) = m.toModuleName(org, name, retrieval)
          if (errors.nonEmpty) {
            System.err.println(s"[ERROR] Error for module [$modules] (${errors.length}):")
            errors.map(e => s"[ERROR]   $e").foreach(System.err.println)
          }
          modules
      }
  }.toSeq

  @transient lazy val AsProvided:       Seq[Dependency] = AllModules.filter(_.shouldBeProvided)
  @transient lazy val ShouldDownload:   Seq[Dependency] = AllModules.filter(_.shouldDownload)
  @transient lazy val NeedToBeReplaced: Seq[Dependency] = AllModules.filter(_.needToBeReplaced)
  @transient lazy val CorrectVersions:  Seq[Dependency] = AllModules.filter(_.isCorrectVersion)
  @transient lazy val PackageKnownRiskDependencies: Map[Dependency, Seq[NameOfModule]] =
    AllModules
      .filter(_.dependenciesToRemove.nonEmpty)
      .map(m => (m, m.dependenciesToRemove))
      .toMap
  @transient lazy val DependenciesOverride: Set[ModuleID] = AllModules
    .map(_.toModuleID)
    .flattenEI
    .toSet
  @transient lazy val ForbiddenModules: Seq[(Dependency, String)] = AllModules
    .filter(_.isForbidden)
    .map(m => (m, m.forbiddenExplanation))
}

private[Config] object SafetyConfiguration {
  implicit val parser: JsonDecode.Parser[SafetyConfiguration] = (input: Map[String, Any]) => {
    for {
      sbtVersion   <- input.getAs[String]("sbtVersion")
      scalaVersion <- input.getAs[List[String]]("scalaVersions")
      _ <- if (scalaVersion.isEmpty) {
        Left("'scalaVersions' cannot be empty")
      } else {
        Right(())
      }
      dockerImage <- input.getOption[String]("dockerImage")
      scalaCFlags <- input.getOption[List[String]]("scalaCFlags")
      moduleRead  <- input.getOption[Map[String, Any]]("modules")
      moduleFinal <- moduleRead match {
        case Some(m) =>
          m.map {
              case (org, ss) =>
                (
                  org,
                  ss.asInstanceOf[Map[String, Any]]
                    .map {
                      case (name, s) =>
                        (name, SerializedModule.parser(org, name)(s.asInstanceOf[Map[String, Any]]))
                    }
                    .toSeq.flattenedEiSeq
                    .map(_.toMap)
                )
            }
            .toSeq.flattenedEiSeq
            .map(_.toMap)
        case None => Right(Map.empty[String, Map[String, SerializedModule]])
      }
    } yield {
      SafetyConfiguration.apply(
        sbtVersion = sbtVersion,
        scalaVersions = scalaVersion.toSet,
        scalaCFlags = scalaCFlags.getOrElse(Nil).toArray,
        dockerImageOpt = dockerImage,
        modules = moduleFinal
      )
    }
  }
}
