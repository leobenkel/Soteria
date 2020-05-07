package com.leobenkel.soteria.Config

import com.leobenkel.soteria.Modules._
import com.leobenkel.soteria.Utils.EitherUtils._
import com.leobenkel.soteria.Utils.Json.JsonDecode
import com.leobenkel.soteria.Utils.Json.JsonParserHelper._
import com.leobenkel.soteria.Utils.LoggerExtended
import com.leobenkel.soteria.Utils.Json.JsonDecode.Encoder
import sbt.librarymanagement.ModuleID

/**
  * What is read from the JSON config file
  */
private[soteria] case class SoteriaConfiguration(
  log:            LoggerExtended,
  sbtVersion:     String,
  scalaVersions:  Set[String],
  scalaCFlags:    Seq[String],
  modules:        Map[String, Map[String, SerializedModule]],
  dockerImageOpt: Option[String]
) extends Encoder {
  @transient lazy private val retrieveModule: String => Either[String, NameOfModule] =
    NameOfModule.find(modules)

  @transient lazy private val RawModules: Seq[(Dependency, Seq[String])] = modules.flatMap {
    case (org, mm) =>
      mm.map {
        case (name, m) =>
          m.toDependency(org, name, retrieveModule)
      }
  }.toSeq

  @transient lazy val AllModules: Seq[Dependency] = RawModules.map {
    case (module, errors) =>
      if (errors.nonEmpty) {
        log.error(s"[ERROR] Error for module [$module] (${errors.length}):")
        errors.map(e => s"[ERROR]   $e").foreach(e => log.error(e))
      }
      module
  }

  @transient lazy val NeedOverridden:   Seq[Dependency] = AllModules.filter(!_.overrideIsEnough)
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
      "sbtVersion"    -> this.sbtVersion,
      "scalaVersions" -> this.scalaVersions.toList,
      "scalaCFlags"   -> this.scalaCFlags.toList,
      "dockerImage"   -> this.dockerImageOpt,
      "modules"       -> this.modules.mapValues(_.mapValues(_.toJsonStructure.right.get))
    )
  }

  def getValidModule(scalaVersion: String): Set[ModuleID] = {
    val scalaV = ScalaV(scalaVersion).right.get
    this.AllModules
      .filter(_.shouldBeDownloaded(scalaV))
      .map(_.toModuleID)
      .flattenEI
      .toSet
  }

  def replaceModule(newModule: Dependency): SoteriaConfiguration = {
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

  object ZTestOnly {
    lazy val RawModulesTest: Seq[(Dependency, Seq[String])] = RawModules
  }
}

private[soteria] object SoteriaConfiguration {
  implicit val parser: LoggerExtended => JsonDecode.Parser[SoteriaConfiguration] =
    (log: LoggerExtended) =>
      (input: Map[String, Any]) => {
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
                            (
                              name,
                              SerializedModule.parser(org, name)(s.asInstanceOf[Map[String, Any]])
                            )
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
          SoteriaConfiguration.apply(
            log,
            sbtVersion = sbtVersion,
            scalaVersions = scalaVersion.toSet,
            scalaCFlags = scalaCFlags.getOrElse(Nil),
            dockerImageOpt = dockerImage,
            modules = moduleFinal
          )
        }
      }
}
