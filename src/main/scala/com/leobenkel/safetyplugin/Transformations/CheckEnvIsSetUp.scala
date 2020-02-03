package com.leobenkel.safetyplugin.Transformations

import com.leobenkel.safetyplugin.SafetyPluginKeys
import sbt.{Def, Task}

private[Transformations] trait CheckEnvIsSetUp {

  def checkEnvVar(envKey: String): Def.Initialize[Task[Unit]] = {
    Def.taskDyn {
      val log = SafetyPluginKeys.safetyGetLog.value
      val value = sys.env.get(envKey)
      Def.task {
        value.fold[Unit] {
          log.fail(s"No value was found for the environment variable: '$envKey'")
        } { _ =>
          log.info(s"A value was found for the environment variable: '$envKey'")
        }
      }
    }
  }
}
