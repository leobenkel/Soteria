sbtPlugin := true

organization := "com.leobenkel"
  homepage := Some(url("https://github.com/leobenkel/safety_plugin"))
  licenses := List("MIT" -> url("https://opensource.org/licenses/MIT"))
developers := List(
    Developer(
      "leobenkel",
      "Leo Benkel",
      "",
      url("https://leobenkel.com")
    )
  )
val projectName = IO.readLines(new File("PROJECT_NAME")).head
name := projectName
