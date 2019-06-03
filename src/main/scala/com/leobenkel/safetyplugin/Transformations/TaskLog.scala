package com.leobenkel.safetyplugin.Transformations

import com.leobenkel.safetyplugin.SafetyPluginKeys
import com.leobenkel.safetyplugin.SafetyPluginKeys.{safetyLogLevel, safetySoft}
import com.leobenkel.safetyplugin.Utils.SafetyLogger
import sbt.Def

private[Transformations] trait TaskLog {
  def safetyGetLogExec(): Def.Initialize[SafetyLogger] = {
    Def.settingDyn {
      val log = SafetyPluginKeys.safetyGetLog.value
      val level = safetyLogLevel.value
      val softError = safetySoft.value
      Def.setting {
        log
          .setLevel(level)
          .setSoftError(softError)
      }
    }
  }
}
