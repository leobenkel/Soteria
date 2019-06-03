package com.leobenkel.safetyplugin.Transformations

import com.leobenkel.safetyplugin.SafetyPluginKeys
import com.leobenkel.safetyplugin.SafetyPluginKeys.{safetyGetLog, safetySoftOnCompilerWarning}
import sbt.{Configuration, Def, Keys, Task}

private[Transformations] trait TaskScalac {
  def extraScalacOptions(conf: Option[Configuration]): Def.Initialize[Task[Seq[String]]] = {
    Def.taskDyn {
      val log = safetyGetLog.value
      val shouldNotFail = safetySoftOnCompilerWarning.value
      val origin = conf.fold(Keys.scalacOptions)(_ / Keys.scalacOptions).value
      val configuration = SafetyPluginKeys.safetyConfig.value

      Def.task {
        log.debug(s"updating 'scalacOptions' (ShouldFail: $shouldNotFail)")
        val compilerFlags = configuration.scalaCFlags

        (
          origin ++
            compilerFlags ++
            (if (shouldNotFail) Seq.empty else Seq("-Xfatal-warnings"))
        ).distinct
      }
    }
  }
}
