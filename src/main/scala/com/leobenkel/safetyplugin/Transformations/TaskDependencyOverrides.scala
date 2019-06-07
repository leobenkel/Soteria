package com.leobenkel.safetyplugin.Transformations

import com.leobenkel.safetyplugin.SafetyPluginKeys
import com.leobenkel.safetyplugin.SafetyPluginKeys.safetyGetLog
import com.leobenkel.safetyplugin.Utils.ImplicitModuleToString._
import sbt.{Configuration, Def, Keys, ModuleID}

private[Transformations] trait TaskDependencyOverrides {

  /**
    * Since this does not inject more libraries into the build but just override,
    * we always override the correct versions,
    * coming from [[com.leobenkel.safetyplugin.Config.SafetyConfiguration.DependenciesOverride]].
    */
  def dependencyOverrides(conf: Option[Configuration]): Def.Initialize[Seq[ModuleID]] = {
    Def.settingDyn {
      val log = safetyGetLog.value
      log.separatorDebug(s"$conf / dependencyOverrides")
      val originalDependencies =
        conf.fold(Keys.dependencyOverrides)(_ / Keys.dependencyOverrides).value
      val config = SafetyPluginKeys.safetyConfig.value

      Def.setting {
        log.debug(s"> Starting with ${originalDependencies.size} dependencyOverrides:")
        val newDependencyOverrides = (originalDependencies ++ config.DependenciesOverride).distinct

        if (conf.isEmpty) {
          log.info(s"> 'dependencyOverrides' have ${newDependencyOverrides.size} overrides.")
          newDependencyOverrides.prettyString(log, "dependencyOverrides")
        } else {
          log.debug(
            s"> '$conf / dependencyOverrides' have " +
              s"${newDependencyOverrides.size} overrides."
          )
        }

        newDependencyOverrides
      }
    }
  }
}
