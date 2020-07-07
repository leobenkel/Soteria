soteriaAddSemantic := false

// Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test

// https://github.com/scoverage/sbt-scoverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.2.7")
//

// ASSEMBLY
addSbtPlugin("com.eed3si9n"      % "sbt-assembly" % "0.14.10")
addSbtPlugin("se.marcuslonnberg" % "sbt-docker"   % "1.5.0")
///

// For testing
addSbtPlugin("io.stryker-mutator" % "sbt-stryker4s" % "0.9.1")

// For SourceClear
// https://github.com/jrudolph/sbt-dependency-graph
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")

// SCALA STYLE

// https://github.com/scalacenter/sbt-scalafix-example/blob/master/project/plugins.sbt
resolvers += Resolver.sonatypeRepo("releases")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.1")

resolvers += Resolver.bintrayRepo("scalameta", "maven")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.0.7")

// http://www.scalastyle.org/sbt.html
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

// TODO: Remove when we are able to use Circe for JSON parsing
val silencerVersion = "1.4.2"
libraryDependencies ++= Seq(
  compilerPlugin("com.github.ghik" %% "silencer-plugin" % silencerVersion),
  "com.github.ghik" %% "silencer-lib" % silencerVersion % Provided
)
