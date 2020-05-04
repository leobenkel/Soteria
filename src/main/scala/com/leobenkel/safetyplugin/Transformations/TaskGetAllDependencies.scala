package com.leobenkel.safetyplugin.Transformations

import com.leobenkel.safetyplugin.Modules.Dependency
import com.leobenkel.safetyplugin.SafetyPluginKeys
import com.leobenkel.safetyplugin.SafetyPluginKeys.safetyGetLog
import com.leobenkel.safetyplugin.Utils.EitherUtils._
import com.leobenkel.safetyplugin.Utils.SafetyLogger
import sbt._

private[Transformations] trait TaskGetAllDependencies {
  lazy protected val javaX = ("javax.ws.rs" % "javax.ws.rs-api" % "2.1")
    .artifacts(Artifact("javax.ws.rs-api", "jar", "jar"))

  protected def processDependencies(
    log:          SafetyLogger,
    config:       Seq[Dependency],
    scalaVersion: String
  ): Seq[ModuleID] = {
    val scalaMainVersion: String = scalaVersion
      .split('.')
      .dropRight(1)
      .mkString(".")

    val allDependenciesTmp = config.map(_.toModuleID)

    allDependenciesTmp
      .filter(_.isLeft)
      .map(_.left.get)
      .foreach(log.debug(_))

    lazy val converter: ModuleID => ModuleID = CrossVersion.apply(scalaVersion, scalaMainVersion)

    val allDependencies = allDependenciesTmp.flattenEI
      .filter {
        case m if (m.name.contains("_") || m.crossVersion.isInstanceOf[CrossVersion.Binary]) =>
          val m2 = converter(m)
          val nameBlocks = m2.name.split("_")
          val moduleScalaVersion = nameBlocks.last
          val output = moduleScalaVersion == scalaMainVersion
          log.debug(
            s"For module '$m2': " +
              s"ModuleScalaVersion: $moduleScalaVersion ; " +
              s"CurrentScalaVersion: $scalaMainVersion ; " +
              s"output: $output"
          )
          output
        case _ => true
      }

    (allDependencies :+ javaX).sortBy(m => (m.organization, m.name))
  }

  def getAllDependencies: Def.Initialize[Seq[ModuleID]] = {
    Def.settingDyn {
      val log = safetyGetLog.value
      val scalaVersion = Keys.scalaVersion.value
      val config = SafetyPluginKeys.safetyConfig.value

      Def.setting(processDependencies(log, config.ShouldDownload, scalaVersion))
    }
  }
}
