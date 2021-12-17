package com.leobenkel.soteria.Transformations

import com.leobenkel.soteria.Modules.{Dependency, ScalaV}
import com.leobenkel.soteria.SoteriaPluginKeys
import com.leobenkel.soteria.SoteriaPluginKeys.soteriaGetLog
import com.leobenkel.soteria.Utils.EitherUtils._
import com.leobenkel.soteria.Utils.SoteriaLogger
import sbt._

private[Transformations] trait TaskGetAllDependencies {
  lazy protected val javaX = ("javax.ws.rs" % "javax.ws.rs-api" % "2.1")
    .artifacts(Artifact("javax.ws.rs-api", "jar", "jar"))

  protected def processDependencies(
      log: SoteriaLogger,
      config: Seq[Dependency],
      scalaVersion: String
  ): Seq[ModuleID] = {
    val scalaV: ScalaV = ScalaV(scalaVersion) match {
      case Left(err) =>
        log.criticalFailure(
          s"Failed to parse ScalaVersion: '$scalaVersion': $err"
        )
        throw new RuntimeException(err)
      case Right(v) => v
    }

    val allDependenciesTmp =
      config.map(c => c.toModuleID.right.map(m => (c, m)))
    allDependenciesTmp.flattenLeft.foreach(log.debug(_))

    val allDependencies: Seq[ModuleID] = allDependenciesTmp.flattenEI
      .filter { case (d, m) => d.shouldBeDownloaded(scalaV, m) }
      .map(_._2)

    (allDependencies :+ javaX).sortBy(m => (m.organization, m.name))
  }

  def getAllDependencies: Def.Initialize[Seq[ModuleID]] = Def.settingDyn {
    val log = soteriaGetLog.value
    val scalaVersion = Keys.scalaVersion.value
    val config = SoteriaPluginKeys.soteriaConfig.value

    Def.setting(processDependencies(log, config.ShouldDownload, scalaVersion))
  }
}
