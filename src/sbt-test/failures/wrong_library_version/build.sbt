lazy val root = (project in file(".")).settings(
  version                                := "0.1",
  scalaVersion                           := "2.12.15",
  assembly / assemblyJarName             := "foo.jar",
  libraryDependencies += "org.scalatest" %% "scalatest" % "0.0.0"
)
