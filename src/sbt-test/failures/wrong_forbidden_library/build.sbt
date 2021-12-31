lazy val root = (project in file(".")).settings(
  version                           := "0.1",
  scalaVersion                      := "2.12.15",
  assembly / assemblyJarName        := "foo.jar",
  libraryDependencies += "io.spray" %% "spray-json" % "1.3.6"
)