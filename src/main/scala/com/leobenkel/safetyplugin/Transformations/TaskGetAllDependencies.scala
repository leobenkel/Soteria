package com.leobenkel.safetyplugin.Transformations

import com.leobenkel.safetyplugin.SafetyPluginKeys
import com.leobenkel.safetyplugin.SafetyPluginKeys.safetyGetLog
import com.leobenkel.safetyplugin.Utils.EitherUtils._
import sbt._

private[Transformations] trait TaskGetAllDependencies {
  def getAllDependencies: Def.Initialize[Seq[ModuleID]] = {
    Def.settingDyn {
      val log = safetyGetLog.value
      val scalaVersion = Keys.scalaVersion.value
      val scalaMainVersion = scalaVersion
        .split('.')
        .dropRight(1)
        .mkString(".")

      val config = SafetyPluginKeys.safetyConfig.value

      val allDependenciesTmp = config.ShouldDownload.map(_.toModuleID)

      allDependenciesTmp
        .filter(_.isLeft)
        .map(_.left.get)
        .foreach(log.debug(_))

      val allDependencies = allDependenciesTmp.flattenEI
        .filter { m =>
          if (m.name.contains("_")) {
            val nameBlocks = m.name.split("_")
            val moduleScalaVersion = nameBlocks.last
            moduleScalaVersion == scalaMainVersion
          } else {
            true
          }
        }

      val javaX = ("javax.ws.rs" % "javax.ws.rs-api" % "2.1")
        .artifacts(Artifact("javax.ws.rs-api", "jar", "jar"))

      Def.setting {
        (allDependencies :+ javaX).sortBy(m => (m.organization, m.name))
      }
    }
  }
}
