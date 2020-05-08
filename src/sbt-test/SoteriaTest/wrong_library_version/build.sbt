lazy val root = (project in file("."))
  .settings(
    version                                := "0.1",
    scalaVersion                           := "2.11.12",
    assemblyJarName in assembly            := "foo.jar",
    libraryDependencies += "org.scalatest" %% "scalatest" % "2.9.0"
  )
