soteriaAddSemantic := false

libraryDependencies += "commons-logging" % "commons-logging" % "1.2"

// Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14" % Test

// https://github.com/sbt/sbt/issues/6997#issuecomment-1310637232
libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

// https://github.com/scoverage/sbt-scoverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.6")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.3.5")
//

// ASSEMBLY
addSbtPlugin("com.eed3si9n"      % "sbt-assembly" % "2.1.0")
addSbtPlugin("se.marcuslonnberg" % "sbt-docker"   % "1.9.0")
///

// For testing
addSbtPlugin("io.stryker-mutator" % "sbt-stryker4s" % "0.15.0")

// For SourceClear
// https://github.com/jrudolph/sbt-dependency-graph
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")

// SCALA STYLE

// https://github.com/scalacenter/sbt-scalafix-example/blob/master/project/plugins.sbt
resolvers ++= Resolver.sonatypeOssRepos("releases")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.10.4")

resolvers += Resolver.bintrayRepo("scalameta", "maven")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.0")

// http://www.scalastyle.org/sbt.html
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

// TODO: Remove when we are able to use Circe for JSON parsing
val silencerVersion = "1.7.12"
ThisBuild / libraryDependencies ++=
  Seq(
    compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full,
  )
