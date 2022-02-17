package com.leobenkel.soteria.Transformations

import com.leobenkel.soteria.SoteriaPluginKeys
import com.leobenkel.soteria.SoteriaPluginKeys.{soteriaLogLevel, soteriaSoft}
import com.leobenkel.soteria.Utils.SoteriaLogger
import sbt.Def

private[Transformations] trait TaskLog {
  def soteriaGetLogExec(): Def.Initialize[SoteriaLogger] =
    Def.settingDyn {
      val log       = SoteriaPluginKeys.soteriaGetLog.value
      val level     = soteriaLogLevel.value
      val softError = soteriaSoft.value
      Def.setting {
        log.setLevel(level).setSoftError(softError)
      }
    }
}
