lazy val root = (project in file(".")).settings(
  version                    := "0.1",
  assembly / assemblyJarName := "foo.jar",
)

soteriaConfPath := {
  val baseDir = baseDirectory.value
  s"file://$baseDir/soteria.json"
}
