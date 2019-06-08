package com.leobenkel.safetyplugin

import com.leobenkel.safetyplugin.Transformations.SafetyExecutionLogic._
import com.leobenkel.safetyplugin.Utils.SafetyLogger
import sbt.util.Level
import sbt.{Def, _}
import sbtassembly._

object SafetyPlugin extends AutoPlugin {

  /**
    * autoImport is a keyword to expose keys
    */
  // scalastyle:off object.name
  object autoImport {
    private val SafetyKeys: SafetyPluginKeys.type = SafetyPluginKeys
    val safetySoft = SafetyKeys.safetySoft
    val safetyLogLevel = SafetyKeys.safetyLogLevel
    val safetyAssemblySettings = SafetyKeys.safetyAssemblySettings
    val safetySoftOnCompilerWarning = SafetyKeys.safetySoftOnCompilerWarning
    val safetyCheckScalaStyle = SafetyKeys.safetyCheckScalaStyle
    val safetyCheckScalaFix = SafetyKeys.safetyCheckScalaFix
    val safetyCheckScalaFmt = SafetyKeys.safetyCheckScalaFmt
    val safetyCheckScalaFmtRun = SafetyKeys.safetyCheckScalaFmtRun
    val safetyCheckScalaCheckAll = SafetyKeys.safetyCheckScalaCheckAll
    val safetyGetAllDependencies = SafetyKeys.safetyGetAllDependencies
    val safetyConfPath = SafetyKeys.safetyConfPath
  }

  // scalastyle:on

  import autoImport._

  override def trigger: PluginTrigger = allRequirements

  override def buildSettings: Seq[Def.Setting[_]] = {
    sys.props += "packaging.type" -> "jar"
    Seq()
  }

  private val scalaStyleSettings: Seq[Def.Setting[_]] = {
    Seq(
      // Scalastyle
      safetyCheckScalaStyle := Def
        .sequential(
          org.scalastyle.sbt.ScalastylePlugin.autoImport.scalastyle.in(Compile).toTask(""),
          org.scalastyle.sbt.ScalastylePlugin.autoImport.scalastyle.in(Test).toTask("")
        ).value,
      // ScalaFix
      safetyCheckScalaFix := Def
        .sequential(
          scalafix.sbt.ScalafixPlugin.autoImport.scalafix.in(Compile).toTask(" --check"),
          scalafix.sbt.ScalafixPlugin.autoImport.scalafix.in(Test).toTask(" --check")
        ).value,
      safetyCheckScalaFmt := Def
        .sequential(
          org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtCheck.in(Compile),
          org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtCheck.in(Test)
        ).value,
      safetyCheckScalaCheckAll := Def
        .sequential(
          safetyCheckScalaFix,
          safetyCheckScalaFmt,
          safetyCheckScalaStyle
        ).value,
      safetyCheckScalaFmtRun := Def
        .sequential(
          org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmt.in(Compile),
          org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmt.in(Test)
        ).value
    ) ++
      // https://stackoverflow.com/a/53824265/3357831
      Vector(addCompilerPlugin(scalafix.sbt.ScalafixPlugin.autoImport.scalafixSemanticdb))
  }

  private val logSettings: Seq[Def.Setting[_]] = {
    Seq(
      // Log
      safetyLogLevel                := Level.Info,
      safetySoft                    := false,
      SafetyPluginKeys.safetyGetLog := SafetyLogger(ConsoleLogger(), Level.Info, softError = false),
      SafetyPluginKeys.safetyGetLog := safetyGetLogExec().value
    )
  }

  private val assemblyRules: Seq[Def.Setting[_]] = {
    import sbtdocker.DockerPlugin.autoImport._
    Seq(
      // Assembly rules
      SafetyPluginKeys.defaultAssemblyOption := defaultAssemblyOptionExec.value,
      safetyAssemblySettings                 := safetyAssemblySettingsExec().value,
      AssemblyKeys.assembly / AssemblyKeys.assemblyJarName := {
        val projectName = Keys.name.value
        val version = Keys.version.value
        s"$projectName-$version-all.jar"
      },
      // Compile Path
      Compile / Keys.scalaSource                 := Keys.baseDirectory.value / "src/main/scala",
      Test / Keys.scalaSource                    := Keys.baseDirectory.value / "src/test/scala",
      AssemblyKeys.assembly / Keys.fullClasspath := (Compile / Keys.fullClasspath).value,
      // Docker
      dockerfile in docker := {
        // The assembly task generates a fat JAR file
        val artifact: File = AssemblyKeys.assembly.value
        val conf = SafetyPluginKeys.safetyConfig.value
        val log = SafetyPluginKeys.safetyGetLog.value
        val artifactTargetPath = s"/app/${artifact.name}"

        conf.dockerImageOpt match {
          case Some(dockerImage) =>
            new sbtdocker.Dockerfile {
              from(dockerImage)
              add(artifact, artifactTargetPath)
              entryPoint("java", "-jar", artifactTargetPath)
            }
          case None =>
            val defaultDockerImage = "openjdk:8-jre"
            log.error(
              s"'dockerImage' was not set in the configuration file. " +
                s"Using Default value: '$defaultDockerImage'."
            )
            new sbtdocker.Dockerfile {
              from(defaultDockerImage)
              add(artifact, artifactTargetPath)
              entryPoint("java", "-jar", artifactTargetPath)
            }
        }
      },
      Keys.version in docker := Keys.version.value,
      buildOptions in docker := sbtdocker.BuildOptions(cache = false)
    )
  }

  private val testSettings: Seq[Def.Setting[_]] = {
    Seq(
      // Only one test at a time ( Easier to read log )
      Keys.testOptions in Test += Tests.Argument("-oD"),
      Keys.javaOptions in Test ++= Seq(
        "-Xms512M",
        "-Xmx2048M",
        "-XX:MaxPermSize=2048M",
        "-XX:+CMSClassUnloadingEnabled"
      ),
      Keys.parallelExecution in Test := false,
      Keys.fork in Test              := true
    )
  }

  private val debugSettings: Seq[Def.Setting[_]] = {
    Seq(
      // For debugging:
      SafetyPluginKeys.safetyDebugWithScala := true,
      SafetyPluginKeys.safetyDebugModule    := None,
      safetyGetAllDependencies              := getAllDependencies.value,
      SafetyPluginKeys.safetyBuildConfig    := checkDependencies(Test).value,
      Keys.commands ++= Seq(debugModuleCommand, debugAllModuleCommand)
    )
  }

  private val configurations: Seq[Def.Setting[_]] = {
    Seq[Def.Setting[_]](
      safetyConfPath                := "./safetyPlugin.json",
      SafetyPluginKeys.safetyConfig := safetyConfigurationExec().value
    )
  }

  override def projectSettings: Seq[Def.Setting[_]] = {
    configurations ++
      logSettings ++
      debugSettings ++
      testSettings ++
      Seq[Def.Setting[_]](
        Compile / Keys.update              := update(Compile).value,
        Test / Keys.update                 := update(Test).value,
        Keys.libraryDependencies           := libraryDependencies(None).value,
        Compile / Keys.libraryDependencies := libraryDependencies(Some(Compile)).value,
        Test / Keys.libraryDependencies    := libraryDependencies(Some(Test)).value,
        Keys.allDependencies               := allDependencies().value,
        Keys.dependencyOverrides           := dependencyOverrides(None).value,
        Compile / Keys.dependencyOverrides := dependencyOverrides(Some(Compile)).value,
        Test / Keys.dependencyOverrides    := dependencyOverrides(Some(Test)).value,
        Compile / Keys.scalaVersion        := scalaVersionExec().value,
        Keys.sbtVersion                    := sbtVersionExec().value,
        // scalac
        safetySoftOnCompilerWarning  := false,
        Keys.scalacOptions           := extraScalacOptions(None).value,
        Compile / Keys.scalacOptions := extraScalacOptions(Some(Compile)).value,
        Test / Keys.scalacOptions    := extraScalacOptions(Some(Test)).value
      ) ++
      assemblyRules ++
      scalaStyleSettings
  }
}
