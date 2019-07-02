package com.leobenkel.safetyplugin.Transformations

import com.leobenkel.safetyplugin.SafetyPluginKeys.safetyConfPath
import com.leobenkel.safetyplugin.{Config, SafetyPluginKeys}
import sbt.Def

private[Transformations] trait TaskConfiguration {
  def safetyConfigurationExec(): Def.Initialize[Config.SafetyConfiguration] = {
    Def.settingDyn {
      val log = SafetyPluginKeys.safetyGetLog.value
      val path = safetyConfPath.value

      Def.setting {
        val conf = Config.ConfigurationParser(log, path)
        log.info(s"Got configuration from '$path'")
        conf.getConf
      }
    }
  }
}
