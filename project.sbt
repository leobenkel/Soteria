sbtPlugin := true

organization := "com.leobenkel"

val v = IO.readLines(new File("VERSION")).head
val projectName = IO.readLines(new File("PROJECT_NAME")).head
name := projectName
version := v
