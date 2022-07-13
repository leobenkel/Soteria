package com.leobenkel.soteria

import com.leobenkel.soteria.SoteriaPluginKeys.FancySettings
import com.leobenkel.soteria.Transformations.SoteriaExecutionLogic
import com.leobenkel.soteria.Utils.SoteriaLogger
import org.scoverage.coveralls.CoverallsPlugin
import sbt.{addCommandAlias, Def, _}
import sbt.util.Level
import sbtassembly._
import scoverage.ScoverageKeys

object SoteriaPlugin extends AutoPlugin with SoteriaExecutionLogic {

  /** autoImport is a keyword to expose keys */
  // scalastyle:off object.name
  object autoImport {
    private val SoteriaKeys: SoteriaPluginKeys.type = SoteriaPluginKeys
    val soteriaSoft                  = SoteriaKeys.soteriaSoft
    val soteriaLogLevel              = SoteriaKeys.soteriaLogLevel
    val soteriaAssemblySettings      = SoteriaKeys.soteriaAssemblySettings
    val soteriaSoftOnCompilerWarning = SoteriaKeys.soteriaSoftOnCompilerWarning
    val soteriaCheckScalaStyle       = SoteriaKeys.soteriaCheckScalaStyle
    val soteriaCheckScalaFix         = SoteriaKeys.soteriaCheckScalaFix
    val soteriaCheckScalaFmt         = SoteriaKeys.soteriaCheckScalaFmt
    val soteriaCheckScalaFmtRun      = SoteriaKeys.soteriaCheckScalaFmtRun
    val soteriaCheckScalaCheckAll    = SoteriaKeys.soteriaCheckScalaCheckAll
    val soteriaGetAllDependencies    = SoteriaKeys.soteriaGetAllDependencies
    val soteriaConfPath              = SoteriaKeys.soteriaConfPath
    val soteriaTestCoverage          = SoteriaKeys.soteriaTestCoverage.setting
    val soteriaSubmitCoverage        = SoteriaKeys.soteriaSubmitCoverage.setting
    val soteriaCheckCoverallEnvVar   = SoteriaKeys.soteriaCheckCoverallEnvVar
    val soteriaDockerImage           = SoteriaKeys.soteriaDockerImage
    val soteriaAddSemantic           = SoteriaKeys.soteriaAddSemantic
  }

  // scalastyle:on

  import autoImport._

  lazy final override val trigger: PluginTrigger = allRequirements

  private def coverallMakeCommand(task: FancySettings[_]): String =
    "; set ThisBuild / coverageEnabled := true " + s"; Test / ${task.nameAsString} " +
      "; set ThisBuild / coverageEnabled := false "

  lazy final override val buildSettings: Seq[Def.Setting[_]] =
    super.buildSettings ++ {
      sys.props += "packaging.type" -> "jar"
      Seq()
    } ++
      addCommandAlias(
        "soteriaRunTestCoverage",
        coverallMakeCommand(SoteriaPluginKeys.soteriaTestCoverage),
      ) ++
      addCommandAlias(
        "soteriaRunSubmitCoverage",
        "; soteriaCheckCoverallEnvVar " +
          coverallMakeCommand(
            SoteriaPluginKeys.soteriaSubmitCoverage
          ),
      )

  lazy private val scalaStyleSettings: Seq[Def.Setting[_]] = {
    Seq(
      // Scalastyle
      soteriaCheckScalaStyle    :=
        Def
          .sequential(
            (Compile / org.scalastyle.sbt.ScalastylePlugin.autoImport.scalastyle).toTask(""),
            (Test / org.scalastyle.sbt.ScalastylePlugin.autoImport.scalastyle).toTask(""),
          )
          .value,
      // ScalaFix
      soteriaCheckScalaFix      :=
        Def
          .sequential(
            (Compile / scalafix.sbt.ScalafixPlugin.autoImport.scalafix).toTask(" --check"),
            (Test / scalafix.sbt.ScalafixPlugin.autoImport.scalafix).toTask(" --check"),
          )
          .value,
      soteriaCheckScalaFmt      :=
        Def
          .sequential(
            Compile / org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtCheck,
            Test / org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtCheck,
          )
          .value,
      soteriaCheckScalaCheckAll :=
        Def
          .sequential(
            soteriaCheckScalaFix,
            soteriaCheckScalaFmt,
            soteriaCheckScalaStyle,
          )
          .value,
      soteriaCheckScalaFmtRun   :=
        Def
          .sequential(
            Compile / org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmt,
            Test / org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmt,
          )
          .value,
      soteriaAddSemantic        := getDefaultAddSemanticValue.value,
      Keys.libraryDependencies  := addScalaFixCompilerPlugin().value,
    )
    /*
     * https://stackoverflow.com/a/53824265/3357831
     * Removed the line below so scala 2.13 is not failing anymore.
     * https://github.com/leobenkel/soteria/issues/37
     * It might have to be added manually if you want to use the Scalafix
     * rewrite feature with scala 2.13.
     *
     * Vector(addCompilerPlugin(scalafix.sbt.ScalafixPlugin.autoImport.scalafixSemanticdb))
     */
  }

  lazy private val logSettings: Seq[Def.Setting[_]] =
    Seq(
      // Log
      soteriaLogLevel                 := Level.Info,
      soteriaSoft                     := false,
      SoteriaPluginKeys.soteriaGetLog :=
        SoteriaLogger(
          ConsoleLogger(),
          Level.Info,
          softError = false,
        ),
      SoteriaPluginKeys.soteriaGetLog := soteriaGetLogExec().value,
    )

  lazy private val assemblyRules: Seq[Def.Setting[_]] = {
    import sbtdocker.DockerPlugin.autoImport._
    Seq(
      // Assembly rules
      SoteriaPluginKeys.defaultAssemblyOption              := defaultAssemblyOptionExec.value,
      soteriaAssemblySettings                              := soteriaAssemblySettingsExec().value,
      AssemblyKeys.assembly / AssemblyKeys.assemblyJarName := {
        val projectName = Keys.name.value
        val version     = Keys.version.value
        s"$projectName-$version-all.jar"
      },
      // Compile Path
      Compile / Keys.scalaSource                           := Keys.baseDirectory.value / "src/main/scala",
      Test / Keys.scalaSource                              := Keys.baseDirectory.value / "src/test/scala",
      AssemblyKeys.assembly / Keys.fullClasspath           :=
        (Compile / Keys.fullClasspath).value,
      // Docker
      soteriaDockerImage                                   := SoteriaPluginKeys.soteriaConfig.value.dockerImage,
      docker / dockerfile                                  := {
        // The assembly task generates a fat JAR file
        val artifact: File = AssemblyKeys.assembly.value
        val conf               = SoteriaPluginKeys.soteriaConfig.value
        val log                = SoteriaPluginKeys.soteriaGetLog.value
        val dockerImage        = soteriaDockerImage.value
        val artifactTargetPath = s"/app/${artifact.name}"

        def makeDocker(imageName: String): Dockerfile =
          new sbtdocker.Dockerfile {
            from(imageName)
            add(artifact, artifactTargetPath)
            entryPoint("java", "-jar", artifactTargetPath)
          }

        if(!conf.dockerImageWasSet) log.error(
          s"'dockerImage' was not set in the configuration file. Using value: '$dockerImage'."
        )

        makeDocker(dockerImage)
      },
      docker / Keys.version                                := Keys.version.value,
      docker / buildOptions                                := sbtdocker.BuildOptions(cache = false),
    )
  }

  lazy private val testSettings: Seq[Def.Setting[_]] = {
    Seq(
      // Only one test at a time ( Easier to read log )
      Test / Keys.testOptions += Tests.Argument("-oD"),
      Test / Keys.javaOptions ++=
        Seq(
          "-Xms512M",
          "-Xmx2048M",
          "-XX:+CMSClassUnloadingEnabled",
        ),
      Test / Keys.parallelExecution := false,
      Test / Keys.fork              := true,
      soteriaCheckCoverallEnvVar    := checkEnvVar("COVERALLS_REPO_TOKEN").value,
      Test / soteriaTestCoverage    :=
        Def
          .sequential(
            Keys.clean,
            Test / Keys.test,
            Test / ScoverageKeys.coverageReport,
            Test / ScoverageKeys.coverageAggregate,
          )
          .value,
      Test / soteriaSubmitCoverage  :=
        Def
          .sequential(
            Test / soteriaTestCoverage,
            Test / CoverallsPlugin.coveralls,
          )
          .value,
    )
  }

  lazy private val debugSettings: Seq[Def.Setting[_]] =
    Seq(
      // For debugging:
      SoteriaPluginKeys.soteriaDebugModule := None,
      soteriaGetAllDependencies            := getAllDependencies.value,
      SoteriaPluginKeys.soteriaBuildConfig := checkDependencies(Test).value,
      Keys.commands ++= Seq(debugModuleCommand, debugAllModuleCommand),
    )

  lazy private val configurations: Seq[Def.Setting[_]] =
    Seq[Def.Setting[_]](
      soteriaConfPath                 := "./soteria.json",
      SoteriaPluginKeys.soteriaConfig := soteriaConfigurationExec().value,
    )

  lazy final override val projectSettings: Seq[Def.Setting[_]] =
    configurations ++ logSettings ++ debugSettings ++ testSettings ++
      Seq[Def.Setting[_]](
        Compile / Keys.update              := update(Compile).value,
        Test / Keys.update                 := update(Test).value,
        Keys.libraryDependencies           := libraryDependenciesSetting(None).value,
        Compile / Keys.libraryDependencies := libraryDependenciesSetting(Some(Compile)).value,
        Test / Keys.libraryDependencies    := libraryDependenciesSetting(Some(Test)).value,
        Keys.allDependencies               := allDependencies(None).value,
        Compile / Keys.allDependencies     := allDependencies(Some(Compile)).value,
        Test / Keys.allDependencies        := allDependencies(Some(Test)).value,
        Keys.dependencyOverrides           := dependencyOverrides(None).value,
        Compile / Keys.dependencyOverrides := dependencyOverrides(Some(Compile)).value,
        Test / Keys.dependencyOverrides    := dependencyOverrides(Some(Test)).value,
        Compile / Keys.scalaVersion        := scalaVersionExecSetting().value,
        Keys.sbtVersion                    := sbtVersionExecSetting().value,
        // scalac
        soteriaSoftOnCompilerWarning       := false,
        Keys.scalacOptions                 := extraScalacOptions(None).value,
        Compile / Keys.scalacOptions       := extraScalacOptions(Some(Compile)).value,
        Test / Keys.scalacOptions          := extraScalacOptions(Some(Test)).value,
      ) ++ assemblyRules ++ scalaStyleSettings
}
