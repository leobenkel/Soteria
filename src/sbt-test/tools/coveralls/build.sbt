lazy val root = (project in file(".")).settings(
  version                    := "0.1",
  assembly / assemblyJarName := "foo.jar",

  // https://github.com/sbt/sbt/issues/6997#issuecomment-1310637232
  libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
)
