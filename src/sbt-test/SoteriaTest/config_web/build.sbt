lazy val root = (project in file("."))
  .settings(
    version                    := "0.1",
    scalaVersion               := "2.11.12",
    assembly / assemblyJarName := "foo.jar"
  )

// TODO: Replace with soteria when merged
soteriaConfPath :=
  "https://raw.githubusercontent.com/leobenkel/soteria/main/soteria.json"
