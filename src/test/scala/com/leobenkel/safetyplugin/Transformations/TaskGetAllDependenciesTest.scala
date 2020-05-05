package com.leobenkel.safetyplugin.Transformations

import com.leobenkel.safetyplugin.Config.SerializedModule
import com.leobenkel.safetyplugin.Modules.{Dependency, ScalaVersionHandler}
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

  test("Process Deps - with scala version filter negative 1") {
    val m = Dependency("org.something" % "artifact-no-cross" % "1.0.0")
      .copy(scalaVersionsFilter = Seq(ScalaVersionHandler("-2.12").right.get))
    val modules = Seq(
      "org.something" % "artifact_2.12" % "1.0.0"
    )
    val output = processDependencies(
      logger,
      modules.map(Dependency(_)) :+ m,
      "2.11.11"
    )

    assertResult(javaX :: m.toModuleID.right.get :: Nil)(output)
  }

  test("Process Deps - with scala version filter positive 1") {
    val m = Dependency("org.something" % "artifact-no-cross" % "1.0.0")
      .copy(scalaVersionsFilter = Seq(ScalaVersionHandler("+2.12").right.get))
    val modules = Seq(
      "org.something" % "artifact_2.12" % "1.0.0"
    )
    val output = processDependencies(
      logger,
      modules.map(Dependency(_)) :+ m,
      "2.11.11"
    )

    assertResult(javaX :: Nil)(output)
  }

  test("Process Deps - with scala version filter negative 2") {
    val m = Dependency("org.something" %% "artifact-no-cross" % "1.0.0")
      .copy(scalaVersionsFilter = Seq(ScalaVersionHandler("-2.12").right.get))
    val modules = Seq(
      "org.something" % "artifact_2.12" % "1.0.0"
    )
    val output = processDependencies(
      logger,
      modules.map(Dependency(_)) :+ m,
      "2.11.11"
    )

    assertResult(javaX :: m.toModuleID.right.get :: Nil)(output)
  }

  test("Process Deps - with scala version filter negative 3") {
    val m = Dependency("org.something" %% "artifact-no-cross" % "1.0.0")
      .copy(scalaVersionsFilter = Seq(ScalaVersionHandler("-2.12").right.get))
    val modules = Seq(
      "org.something" % "artifact_2.12" % "1.0.0"
    )
    val output = processDependencies(
      logger,
      modules.map(Dependency(_)) :+ m,
      "2.12.11"
    )

    assertResult(javaX +: modules)(output)
  }

  test("Process Deps - with scala version filter positive 2") {
    val m = Dependency("org.something" %% "artifact-no-cross" % "1.0.0")
      .copy(scalaVersionsFilter = Seq(ScalaVersionHandler("+2.12").right.get))
    val modules = Seq(
      "org.something" % "artifact_2.12" % "1.0.0"
    )
    val output = processDependencies(
      logger,
      modules.map(Dependency(_)) :+ m,
      "2.11.11"
    )

    assertResult(javaX :: Nil)(output)
  }

  test("Process deps - test exclusion 1") {
    val m1 = SerializedModule
      .parser("com.test", "artifact-test")(
        Map[String, Any](
          "dependenciesToRemove" -> List(
            "com.fasterxml.jackson.core | jackson-annotations",
            "com.fasterxml.jackson.core | jackson-core",
            "com.fasterxml.jackson.core | jackson-databind"
          ),
          "version" -> "3.3.1"
        )
      ).right.get
      .toDependency("com.test", "artifact-test", _ => Left("too bad"))._1
    val output11 = processDependencies(
      logger,
      Seq(m1),
      "2.11.7"
    )
    assertResult(m1.toModuleID.right.get :: javaX :: Nil)(output11)
    val output12 = processDependencies(
      logger,
      Seq(m1),
      "2.12.11"
    )
    assertResult(m1.toModuleID.right.get :: javaX :: Nil)(output12)
  }

  test("Process deps - test exclusion 2") {
    val m1 = SerializedModule
      .parser("com.test", "artifact-test")(
        Map[String, Any](
          "dependenciesToRemove" -> List(
            "com.fasterxml.jackson.core | jackson-annotations",
            "com.fasterxml.jackson.core | jackson-core",
            "com.fasterxml.jackson.core | jackson-databind"
          ),
          "version"             -> "3.3.1",
          "scalaVersionsFilter" -> List("+2.12")
        )
      ).right.get
      .toDependency("com.test", "artifact-test", _ => Left("too bad"))._1
    val output11 = processDependencies(
      logger,
      Seq(m1),
      "2.11.7"
    )
    assertResult(javaX :: Nil)(output11)
    val output12 = processDependencies(
      logger,
      Seq(m1),
      "2.12.11"
    )
    assertResult(m1.toModuleID.right.get :: javaX :: Nil)(output12)
  }
}
