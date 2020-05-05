package com.leobenkel.safetyplugin.Transformations

import com.leobenkel.safetyplugin.Modules.{Dependency, ScalaV}
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
    val scalaV: ScalaV = ScalaV(scalaVersion) match {
      case Left(err) =>
        log.criticalFailure(s"Failed to parse ScalaVersion: '$scalaVersion': $err")
        throw new RuntimeException(err)
      case Right(v) => v
    }

    val allDependenciesTmp = config.map(c => c.toModuleID.right.map(m => (c, m)))
    allDependenciesTmp.flattenLeft.foreach(log.debug(_))

    val allDependencies: Seq[ModuleID] = allDependenciesTmp.flattenEI
      .filter { case (d, m) => d.shouldBeDownloaded(scalaV, m) }
      .map(_._2)

    (allDependencies :+ javaX).sortBy(m => (m.organization, m.name))
  }

  def getAllDependencies: Def.Initialize[Seq[ModuleID]] = Def.settingDyn {
    val log = safetyGetLog.value
    val scalaVersion = Keys.scalaVersion.value
    val config = SafetyPluginKeys.safetyConfig.value

    Def.setting(processDependencies(log, config.ShouldDownload, scalaVersion))
  }
}
