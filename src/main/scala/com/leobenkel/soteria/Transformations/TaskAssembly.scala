package com.leobenkel.soteria.Transformations

import com.eed3si9n.jarjarabrams.ShadeRule
import com.leobenkel.soteria.SoteriaPluginKeys.{defaultAssemblyOption, soteriaGetLog}
import sbt._
import sbtassembly._

private[Transformations] trait TaskAssembly extends MergeStrategyConfiguration {
  def defaultAssemblyOptionExec: Def.Initialize[Task[AssemblyOption]] = {
    Def.taskDyn {
      Def.task {
        val s = Keys.streams.value
        AssemblyOption(
          assemblyDirectory = Some(s.cacheDirectory / "assembly"),
          includeBin = (Keys.packageBin / AssemblyKeys.assembleArtifact).value,
          includeScala = (AssemblyKeys.assemblyPackageScala / AssemblyKeys.assembleArtifact).value,
          includeDependency =
            (AssemblyKeys.assemblyPackageDependency / AssemblyKeys.assembleArtifact).value,
          mergeStrategy = (AssemblyKeys.assembly / AssemblyKeys.assemblyMergeStrategy).value,
          excludedJars = (AssemblyKeys.assembly / AssemblyKeys.assemblyExcludedJars).value,
          excludedFiles = Assembly.defaultExcludedFiles,
          cacheOutput = true,
          cacheUnzip = true,
          appendContentHash = false,
          prependShellScript = None,
          maxHashLength = None,
          shadeRules = (AssemblyKeys.assembly / AssemblyKeys.assemblyShadeRules).value,
          level = (AssemblyKeys.assembly / Keys.logLevel).value,
          scalaVersion = Keys.scalaVersion.value,
        )
      }
    }
  }

  def soteriaAssemblySettingsExec(): Def.Initialize[Task[AssemblyOption]] =
    Def.taskDyn {
      val log = soteriaGetLog.value
      val oldStrategy: String => MergeStrategy =
        (AssemblyKeys.assembly / AssemblyKeys.assemblyMergeStrategy).value
      val assemblyOption = defaultAssemblyOption.value
      log.info(s"Overriding assembly settings")

      Def.task {
        val mergeStrategy = (input: String) => getMergeStrategy(input, oldStrategy)
        val shadeRule: Seq[ShadeRule] =
          Seq(ShadeRule.rename("com.google.common.**" -> "shade.@0").inAll)

        assemblyOption
          .withIncludeScala(includeScala = false)
          .withIncludeDependency(includeDependency = true)
          .withShadeRules(shadeRule)
          .withMergeStrategy(mergeStrategy = mergeStrategy)
      }
    }
}
