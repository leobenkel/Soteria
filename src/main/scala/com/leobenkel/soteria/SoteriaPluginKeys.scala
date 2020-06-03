package com.leobenkel.soteria

import com.leobenkel.soteria.Config.SoteriaConfiguration
import com.leobenkel.soteria.Utils.SoteriaLogger
import com.leobenkel.soteria.Utils.SoteriaLogger
import sbt.librarymanagement.ModuleID
import sbt.util.Level
import sbt.{TaskKey, settingKey, taskKey}
import sbtassembly.AssemblyOption

private[soteria] object SoteriaPluginKeys {
  case class FancySettings[A](
    setting:      A,
    nameAsString: String
  )

  // Upper case SettingKey and TaskKey does not work.
  // Use settingKey and taskKey.

  lazy val soteriaGetLog = settingKey[SoteriaLogger]("private")

  lazy val soteriaConfPath = settingKey[String]("Path to the configuration file.")
  lazy val soteriaConfig = settingKey[SoteriaConfiguration]("private")
  lazy val soteriaAssemblySettings = taskKey[AssemblyOption](
    "Use to set the Assemble option to the right values"
  )

  lazy val soteriaGetAllDependencies = settingKey[Seq[ModuleID]](
    "Will return the list of all the dependencies known."
  )

  lazy val defaultAssemblyOption = taskKey[AssemblyOption]("private")

  lazy val soteriaSoftOnCompilerWarning = settingKey[Boolean](
    "If true, will not fail compilation on compiler warning."
  )

  lazy val soteriaBuildConfig = taskKey[SoteriaConfiguration]("Used for command. Do not call")

  lazy val soteriaDockerImage = settingKey[String](
    "The docker image from the configuration " +
      "and use to build with sbt-docker"
  )

  lazy val soteriaDebugModule = settingKey[Option[ModuleID]](
    "When set, will print out the dependency of this module."
  )

  lazy val soteriaSoft = settingKey[Boolean](
    "If true, won't fail compilation but throw warnings."
  )

  lazy val soteriaLogLevel = settingKey[Level.Value](
    "Set the level of logs for the soteria Plugin."
  )

  lazy val soteriaAddSemantic = settingKey[Boolean](
    "If true, semanticdb will be added"
  )

  // Scala style
  lazy val soteriaCheckScalaStyle = taskKey[Unit]("Run ScalaStyle.")
  lazy val soteriaCheckScalaFix = taskKey[Unit]("Run ScalaFix.")
  lazy val soteriaCheckScalaFmt = taskKey[Boolean]("Run ScalaFmtCheck.")
  lazy val soteriaCheckScalaCheckAll = taskKey[Unit]("Check all scala style.")
  lazy val soteriaCheckScalaFmtRun = taskKey[Unit]("Run ScalaFmt.")

  // coverall
  lazy val soteriaTestCoverage: FancySettings[TaskKey[Unit]] = FancySettings(
    setting = taskKey[Unit]("PRIVATE - Run test coverage"),
    nameAsString = "soteriaTestCoverage"
  )
  lazy val soteriaSubmitCoverage: FancySettings[TaskKey[Unit]] = FancySettings(
    setting = taskKey[Unit]("PRIVATE - Run test coverage and submit coverage"),
    nameAsString = "soteriaSubmitCoverage"
  )
  lazy val soteriaCheckCoverallEnvVar = taskKey[Unit]("PRIVATE")
}
