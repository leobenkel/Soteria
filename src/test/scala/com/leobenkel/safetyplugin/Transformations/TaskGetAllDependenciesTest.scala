package com.leobenkel.safetyplugin.Transformations

import com.leobenkel.safetyplugin.Modules.Dependency
import com.leobenkel.safetyplugin.ParentTest
import com.leobenkel.safetyplugin.Utils.SafetyLogger
import sbt._
import sbt.internal.util.ConsoleLogger
import sbt.util.Level

class TaskGetAllDependenciesTest extends ParentTest with TaskGetAllDependencies {
  lazy val logger: SafetyLogger = SafetyLogger(
    ConsoleLogger(),
    Level.Debug,
    softError = true
  )

  test("Process Deps - all good - 2.12") {
    val modules = Seq(
      "org.something" %% "artifact"         % "1.0.0",
      "org.something" % "artifact-no-cross" % "1.0.0"
    )
    val output = processDependencies(
      logger,
      modules.map(Dependency(_)),
      "2.12.11"
    )

    assertResult(javaX +: modules)(output)
  }

  test("Process Deps - all good - 2.11") {
    val modules = Seq(
      "org.something" %% "artifact"         % "1.0.0",
      "org.something" % "artifact-no-cross" % "1.0.0"
    )
    val output = processDependencies(
      logger,
      modules.map(Dependency(_)),
      "2.11.11"
    )

    assertResult(javaX +: modules)(output)
  }

  test("Process Deps - filter bad - 2.11") {
    val m = "org.something" % "artifact-no-cross" % "1.0.0"
    val modules = Seq(
      "org.something" % "artifact_2.12" % "1.0.0",
      m
    )
    val output = processDependencies(
      logger,
      modules.map(Dependency(_)),
      "2.11.11"
    )

    assertResult(javaX :: m :: Nil)(output)
  }
}
