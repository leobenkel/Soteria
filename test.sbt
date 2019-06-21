// https://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html

enablePlugins(SbtPlugin)

scriptedLaunchOpts ++= Seq("-Dplugin.version=" + version.value)

scriptedBufferLog := false

logLevel in stryker := Level.Debug

safetySoftOnCompilerWarning in stryker := true
