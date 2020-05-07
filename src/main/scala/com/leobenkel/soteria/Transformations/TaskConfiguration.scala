package com.leobenkel.soteria.Transformations

import com.leobenkel.soteria.SoteriaPluginKeys.soteriaConfPath
import com.leobenkel.soteria.{Config, SoteriaPluginKeys}
import com.leobenkel.soteria.SoteriaPluginKeys
import sbt.Def

private[Transformations] trait TaskConfiguration {
  def soteriaConfigurationExec(): Def.Initialize[Config.SoteriaConfiguration] = {
    Def.settingDyn {
      val log = SoteriaPluginKeys.soteriaGetLog.value
      val path = soteriaConfPath.value

      Def.setting {
        val conf = Config.ConfigurationParser(log, path)
        log.info(s"Got configuration from '$path'")
        conf.getConf
      }
    }
  }
}
