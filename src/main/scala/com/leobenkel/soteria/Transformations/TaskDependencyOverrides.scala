package com.leobenkel.soteria.Transformations

import com.leobenkel.soteria.SoteriaPluginKeys
import com.leobenkel.soteria.SoteriaPluginKeys.soteriaGetLog
import com.leobenkel.soteria.Utils.ImplicitModuleToString._
import sbt.{Configuration, Def, Keys, ModuleID}

private[Transformations] trait TaskDependencyOverrides {

  /**
   * Since this does not inject more libraries into the build but just override, we always override
   * the correct versions.
   */
  def dependencyOverrides(
      conf: Option[Configuration]
  ): Def.Initialize[Seq[ModuleID]] = {
    Def.settingDyn {
      val log                  = soteriaGetLog.value
      log.separatorDebug(s"$conf / dependencyOverrides")
      val originalDependencies =
        conf.fold(Keys.dependencyOverrides)(_ / Keys.dependencyOverrides).value
      val config               = SoteriaPluginKeys.soteriaConfig.value

      Def.setting {
        log.debug(
          s"> Starting with ${originalDependencies.size} dependencyOverrides:"
        )
        val newDependencyOverrides = (originalDependencies ++ config.DependenciesOverride).distinct

        if(conf.isEmpty) {
          log.info(
            s"> 'dependencyOverrides' have ${newDependencyOverrides.size} overrides."
          )
          newDependencyOverrides.prettyString(log, "dependencyOverrides")
        } else log.debug(
          s"> '$conf / dependencyOverrides' have " + s"${newDependencyOverrides.size} overrides."
        )

        newDependencyOverrides
      }
    }
  }
}
