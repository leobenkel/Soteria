package com.leobenkel.safetyplugin

import com.leobenkel.safetyplugin.Config.SafetyConfiguration
import com.leobenkel.safetyplugin.Utils.SafetyLogger
import sbt.librarymanagement.ModuleID
import sbt.util.Level
import sbt.{TaskKey, settingKey, taskKey}
import sbtassembly.AssemblyOption

private[safetyplugin] object SafetyPluginKeys {
  case class FancySettings[A](
    setting:      A,
    nameAsString: String
  )

  // Upper case SettingKey and TaskKey does not work.
  // Use settingKey and taskKey.

  lazy val safetyGetLog = settingKey[SafetyLogger]("private")

  lazy val safetyConfPath = settingKey[String]("Path to the configuration file.")
  lazy val safetyConfig = settingKey[SafetyConfiguration]("private")
  lazy val safetyAssemblySettings = taskKey[AssemblyOption](
    "Use to set the Assemble option to the right values"
  )

  lazy val safetyGetAllDependencies = settingKey[Seq[ModuleID]](
    "Will return the list of all the dependencies known."
  )

  lazy val defaultAssemblyOption = taskKey[AssemblyOption]("private")

  lazy val safetySoftOnCompilerWarning = settingKey[Boolean](
    "If true, will not fail compilation on compiler warning."
  )

  lazy val safetyBuildConfig = taskKey[SafetyConfiguration]("Used for command. Do not call")

  lazy val safetyDebugModule = settingKey[Option[(String, String)]](
    "When set, will print out the dependency of this module."
  )

  lazy val safetySoft = settingKey[Boolean](
    "If true, won't fail compilation but throw warnings."
  )

  lazy val safetyLogLevel = settingKey[Level.Value](
    "Set the level of logs for the safety Plugin."
  )

  // Scala style
  lazy val safetyCheckScalaStyle = taskKey[Unit]("Run ScalaStyle.")
  lazy val safetyCheckScalaFix = taskKey[Unit]("Run ScalaFix.")
  lazy val safetyCheckScalaFmt = taskKey[Boolean]("Run ScalaFmtCheck.")
  lazy val safetyCheckScalaCheckAll = taskKey[Unit]("Check all scala style.")
  lazy val safetyCheckScalaFmtRun = taskKey[Unit]("Run ScalaFmt.")

  // coverall
  lazy val safetyTestCoverage: FancySettings[TaskKey[Unit]] = FancySettings(
    setting = taskKey[Unit]("PRIVATE - Run test coverage"),
    nameAsString = "safetyTestCoverage"
  )
  lazy val safetySubmitCoverage: FancySettings[TaskKey[Unit]] = FancySettings(
    setting = taskKey[Unit]("PRIVATE - Run test coverage and submit coverage"),
    nameAsString = "safetySubmitCoverage"
  )
  lazy val safetyCheckCoverallEnvVar = taskKey[Unit]("PRIVATE")
}
