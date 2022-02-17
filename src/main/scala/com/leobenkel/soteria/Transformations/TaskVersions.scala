package com.leobenkel.soteria.Transformations

import com.leobenkel.soteria.SoteriaPluginKeys
import com.leobenkel.soteria.SoteriaPluginKeys.soteriaGetLog
import sbt.{Def, Keys, Task}

private[Transformations] trait TaskVersions {

  /** Will check that SBT is the correct version. */
  def sbtVersionExec(): Def.Initialize[Task[String]] =
    Def.taskDyn {
      val log = soteriaGetLog.value
      val sbtVersion: String = Keys.sbtVersion.value
      val configuration = SoteriaPluginKeys.soteriaConfig.value

      Def.task {
        val legalSbtVersion = configuration.sbtVersion

        if(sbtVersion != legalSbtVersion) log.fail(s"SBT: $sbtVersion != $legalSbtVersion !!!")
        else log.debug(s"SBT: $sbtVersion (correct)")

        sbtVersion
      }
    }

  /** Will check that SBT is the correct version. */
  def sbtVersionExecSetting(): Def.Initialize[String] =
    Def.settingDyn {
      val log = soteriaGetLog.value.setSoftError(true)
      val sbtVersion: String = Keys.sbtVersion.value
      val configuration = SoteriaPluginKeys.soteriaConfig.value

      Def.setting {
        val legalSbtVersion = configuration.sbtVersion

        if(sbtVersion != legalSbtVersion) log.fail(s"SBT: $sbtVersion != $legalSbtVersion !!!")
        else log.debug(s"SBT: $sbtVersion (correct)")

        sbtVersion
      }
    }

  /** Will check that Scala is the correct version. */
  def scalaVersionExec(): Def.Initialize[Task[String]] =
    Def.taskDyn {
      val log = soteriaGetLog.value
      val scalaVersion: String = Keys.scalaVersion.value
      val configuration = SoteriaPluginKeys.soteriaConfig.value

      Def.task {
        val legalScalaVersion = configuration.scalaVersions

        if(legalScalaVersion.contains(scalaVersion)) log.debug(s"Scala: $scalaVersion (correct)")
        else log.fail(s"Scala: $scalaVersion != [${legalScalaVersion.mkString(" OR ")}] !!!")

        scalaVersion
      }
    }

  /** Will check that Scala is the correct version. */
  def scalaVersionExecSetting(): Def.Initialize[String] =
    Def.settingDyn {
      val log = soteriaGetLog.value.setSoftError(true)
      val scalaVersion: String = Keys.scalaVersion.value
      val configuration = SoteriaPluginKeys.soteriaConfig.value

      Def.setting {
        val legalScalaVersion = configuration.scalaVersions

        if(legalScalaVersion.contains(scalaVersion)) log.debug(s"Scala: $scalaVersion (correct)")
        else log.fail(s"Scala: $scalaVersion != [${legalScalaVersion.mkString(" OR ")}] !!!")

        scalaVersion
      }
    }
}
