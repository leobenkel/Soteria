package com.leobenkel.soteria.Transformations

import com.leobenkel.soteria.SoteriaPluginKeys
import com.leobenkel.soteria.SoteriaPluginKeys.{soteriaGetLog, soteriaSoftOnCompilerWarning}
import sbt.{Configuration, Def, Keys, Task}

private[Transformations] trait TaskScalac {
  def extraScalacOptions(
    conf: Option[Configuration]
  ): Def.Initialize[Task[Seq[String]]] =
    Def.taskDyn {
      val log = soteriaGetLog.value
      val shouldNotFail = soteriaSoftOnCompilerWarning.value
      val origin = conf.fold(Keys.scalacOptions)(_ / Keys.scalacOptions).value
      val configuration = SoteriaPluginKeys.soteriaConfig.value

      Def.task {
        log.debug(s"updating 'scalacOptions' (ShouldFail: $shouldNotFail)")
        val compilerFlags = configuration.scalaCFlags

        (
          origin ++ compilerFlags ++ (if (shouldNotFail) Seq.empty else Seq("-Xfatal-warnings"))
        ).distinct
      }
    }
}
