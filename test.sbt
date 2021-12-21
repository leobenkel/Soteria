import org.scoverage.coveralls.Imports.CoverallsKeys.coverallsFailBuildOnError

// https://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html

enablePlugins(SbtPlugin)

scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
scriptedBufferLog         := false
scriptedParallelInstances := 1
scriptedBatchExecution    := false

stryker / logLevel := Level.Debug

coverageOutputDebug := true

coverallsFailBuildOnError := false
