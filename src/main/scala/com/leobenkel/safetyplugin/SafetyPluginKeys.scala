package com.leobenkel.safetyplugin

import com.leobenkel.safetyplugin.Config.SafetyConfiguration
import com.leobenkel.safetyplugin.Utils.SafetyLogger
import sbt.librarymanagement.ModuleID
import sbt.util.Level
import sbt.{settingKey, taskKey}
import sbtassembly.AssemblyOption

private[safetyplugin] object SafetyPluginKeys {
  // Upper case SettingKey and TaskKey does not work.
  // Use settingKey and taskKey.

  val safetyGetLog = settingKey[SafetyLogger]("private")

  val safetyConfPath = settingKey[String]("Path to the configuration file.")
  val safetyConfig = settingKey[SafetyConfiguration]("private")
  val safetyAssemblySettings = taskKey[AssemblyOption](
    "Use to set the Assemble option to the right values"
  )

  val safetyGetAllDependencies = settingKey[Seq[ModuleID]](
    "Will return the list of all the dependencies known."
  )

  val defaultAssemblyOption = taskKey[AssemblyOption]("private")

  val safetySoftOnCompilerWarning = settingKey[Boolean](
    "If true, will not fail compilation on compiler warning."
  )

  val safetyBuildConfig = taskKey[SafetyConfiguration]("Used for command. Do not call")

  val safetyDebugModule = settingKey[Option[(String, String)]](
    "When set, will print out the dependency of this module."
  )

  val safetySoft = settingKey[Boolean](
    "If true, won't fail compilation but throw warnings."
  )

  val safetyLogLevel = settingKey[Level.Value](
    "Set the level of logs for the safety Plugin."
  )

  // Scala style
  val safetyCheckScalaStyle = taskKey[Unit]("Run ScalaStyle.")
  val safetyCheckScalaFix = taskKey[Unit]("Run ScalaFix.")
  val safetyCheckScalaFmt = taskKey[Boolean]("Run ScalaFmtCheck.")
  val safetyCheckScalaCheckAll = taskKey[Unit]("Check all scala style.")
  val safetyCheckScalaFmtRun = taskKey[Unit]("Run ScalaFmt.")
}
