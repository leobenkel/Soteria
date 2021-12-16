package com.leobenkel.soteria.Transformations

import com.leobenkel.soteria.SoteriaPluginKeys
import sbt._

import scala.util.matching.Regex

private[Transformations] trait AddScalaFixCompilerPlugin {

  private[Transformations] def shouldAddCompilerPlugin(
      scalaVersion: String
  ): Boolean = {
    val pattern: Regex = "^2\\.1[012]\\..*$".r
    pattern.pattern.matcher(scalaVersion).find()
  }

  def getDefaultAddSemanticValue: Def.Initialize[Boolean] = {
    Def.settingDyn {
      val scalaVersion = Keys.scalaVersion.value
      Def.setting(shouldAddCompilerPlugin(scalaVersion))
    }
  }

  def addScalaFixCompilerPlugin(): Def.Initialize[Seq[ModuleID]] = {
    Def.settingDyn {
      val soteriaAddSemantic = SoteriaPluginKeys.soteriaAddSemantic.value
      val libraries = Keys.libraryDependencies.value

      Def.setting {
        if (soteriaAddSemantic) {
          libraries :+ compilerPlugin(
            scalafix.sbt.ScalafixPlugin.autoImport.scalafixSemanticdb
          )
        } else {
          libraries
        }
      }
    }
  }
}
