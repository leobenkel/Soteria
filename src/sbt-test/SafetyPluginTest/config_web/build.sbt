lazy val root = (project in file("."))
  .settings(
    version                     := "0.1",
    scalaVersion                := "2.11.12",
    assemblyJarName in assembly := "foo.jar"
  )

safetyConfPath :=
  "https://raw.githubusercontent.com/leobenkel/safety_plugin/master/safetyPlugin.json"
