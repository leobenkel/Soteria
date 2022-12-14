// To publish
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.10")

// https://github.com/scoverage/sbt-scoverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.6")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.3.5")

// https://github.com/sbt/sbt/issues/6997#issuecomment-1310637232
ThisBuild / libraryDependencySchemes +=
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
