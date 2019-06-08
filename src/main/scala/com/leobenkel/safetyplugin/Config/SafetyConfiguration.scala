package com.leobenkel.safetyplugin.Config

import com.leobenkel.safetyplugin.Modules.{Dependency, NameOfModule}
import com.leobenkel.safetyplugin.Utils.EitherUtils._
import com.leobenkel.safetyplugin.Utils.Json.JsonDecode
import com.leobenkel.safetyplugin.Utils.Json.JsonParserHelper._
import sbt.librarymanagement.ModuleID

/**
  * What is read from the JSON config file
  */
private[safetyplugin] case class SafetyConfiguration(
  sbtVersion:     String,
  scalaVersions:  Set[String],
  scalaCFlags:    Seq[String],
  modules:        Map[String, Map[String, SerializedModule]],
  dockerImageOpt: Option[String]
) extends JsonDecode.Encoder {
  @transient lazy private val retrieveModule: String => Either[String, NameOfModule] =
    NameOfModule.find(modules)
  @transient lazy val AllModules: Seq[Dependency] = modules.flatMap {
    case (org, mm) =>
      mm.map {
        case (name, m) =>
          val (modules, errors) = m.toDependency(org, name, retrieveModule)
          if (errors.nonEmpty) {
            System.err.println(s"[ERROR] Error for module [$modules] (${errors.length}):")
            errors.map(e => s"[ERROR]   $e").foreach(System.err.println)
          }
          modules
      }
  }.toSeq

  @transient lazy val NeedOverriden:    Seq[Dependency] = AllModules.filter(!_.overrideIsEnough)
  @transient lazy val AsProvided:       Seq[Dependency] = AllModules.filter(_.shouldBeProvided)
  @transient lazy val ShouldDownload:   Seq[Dependency] = AllModules.filter(_.shouldDownload)
  @transient lazy val NeedToBeReplaced: Seq[Dependency] = AllModules.filter(_.needToBeReplaced)
  @transient lazy val CorrectVersions:  Seq[Dependency] = AllModules.filter(_.isCorrectVersion)
  @transient lazy val PackageKnownRiskDependencies: Map[Dependency, Seq[NameOfModule]] =
    AllModules
      .filter(_.dependenciesToRemove.nonEmpty)
      .map(m => (m, m.dependenciesToRemove))
      .toMap
  @transient lazy val AllModuleID: Set[ModuleID] = AllModules
    .map(_.toModuleID)
    .flattenEI
    .toSet
  @transient lazy val DependenciesOverride: Set[ModuleID] = AllModuleID
  @transient lazy val ForbiddenModules: Seq[(Dependency, String)] = AllModules
    .filter(_.isForbidden)
    .map(m => (m, m.forbiddenExplanation))

  lazy override protected val asMap: Map[String, Any] = {
    Map[String, Any](
      "sbtVersion"     -> this.sbtVersion,
      "scalaVersions"  -> this.scalaVersions.toList,
      "scalaCFlags"    -> this.scalaCFlags.toList,
      "dockerImageOpt" -> this.dockerImageOpt,
      "modules"        -> this.modules.mapValues(_.mapValues(_.toJsonStructure.right.get))
    )
  }

//  def replaceModules(newModule: Map[String, Map[String, SerializedModule]]): SafetyConfiguration = {
//
//    type MapType = Map[String, Any]
//
//    def mergeValues(
//      o1: Any,
//      o2: Any
//    ): Any = {
//      (o1, o2) match {
//        case (v1: MapType, v2: MapType) =>
//          merge(v1, v2)
//        case (_, v2) => v2
//      }
//    }
//
//    def merge(
//      map1: Map[String, Any],
//      map2: Map[String, Any]
//    ): Map[String, Any] = {
//      (map1.keySet ++ map2.keySet).map { key =>
//        key -> ((map1.get(key), map2.get(key)) match {
//          case (Some(v1), Some(v2)) => mergeValues(v1, v2)
//          case (None, Some(v2))     => v2
//          case (Some(v1), None)     => v1
//        })
//      }.toMap
//    }
//
//    this.copy(
//      modules = merge(modules, newModule)
//        .mapValues(_.asInstanceOf[Map[String, Any]].mapValues(_.asInstanceOf[SerializedModule]))
//    )
//  }

  def replaceModules(newModule: Dependency): SafetyConfiguration = {
    this.copy(
      modules = modules.updated(
        newModule.organization,
        modules
          .get(newModule.organization)
          .map(_.updated(newModule.name, newModule.toSerializedModule))
          .getOrElse(Map(newModule.name -> newModule.toSerializedModule))
      )
    )
  }
}

private[safetyplugin] object SafetyConfiguration {
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
        scalaCFlags = scalaCFlags.getOrElse(Nil),
        dockerImageOpt = dockerImage,
        modules = moduleFinal
      )
    }
  }
}
