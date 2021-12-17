lazy val root = (project in file(".")).settings(
  version                    := "0.1",
  scalaVersion               := "2.12.14",
  assembly / assemblyJarName := "foo.jar"
)

// TODO: Replace with main branch when merged
soteriaConfPath := "https://raw.githubusercontent.com/leobenkel/soteria/main/soteria.json"
