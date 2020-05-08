lazy val root = (project in file("."))
  .settings(
    version                     := "0.1",
    scalaVersion                := "2.11.12",
    assemblyJarName in assembly := "foo.jar"
  )

// TODO: Replace with soteria when merged
soteriaConfPath :=
  "https://raw.githubusercontent.com/leobenkel/soteria/master/soteria.json"
