// https://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html

enablePlugins(SbtPlugin)

scriptedLaunchOpts ++= Seq("-Dplugin.version=" + version.value)

scriptedBufferLog := false

// TODO: Remove those when plugin is added to itself
Keys.testOptions in Test += Tests.Argument("-oD")
Keys.javaOptions in Test ++= Seq(
  "-Xms512M",
  "-Xmx2048M",
  "-XX:MaxPermSize=2048M",
  "-XX:+CMSClassUnloadingEnabled"
)
Keys.parallelExecution in Test := false
Keys.fork in Test              := true
logLevel in stryker            := Level.Debug
