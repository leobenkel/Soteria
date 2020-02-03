package com.leobenkel.safetyplugin.Transformations

import sbt._

import scala.util.matching.Regex

private[Transformations] trait AddScalaFixCompilerPlugin {

  private[Transformations] def shouldAddCompilerPlugin(scalaVersion: String): Boolean = {
    val pattern: Regex = "^2\\.1[012]\\..*$".r
    pattern.pattern.matcher(scalaVersion).find()
  }

  def addScalaFixCompilerPlugin(): Def.Initialize[Seq[ModuleID]] = {
    Def.settingDyn {
      val scalaVersion = Keys.scalaVersion.value
      val libraries = Keys.libraryDependencies.value

      Def.setting {
        if (shouldAddCompilerPlugin(scalaVersion)) {
          libraries :+ compilerPlugin(scalafix.sbt.ScalafixPlugin.autoImport.scalafixSemanticdb)
        } else {
          libraries
        }
      }
    }
  }
}
