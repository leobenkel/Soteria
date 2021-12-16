package com.leobenkel.soteria.Transformations

import com.leobenkel.soteria.Config.{SerializedModule, SoteriaConfiguration}
import com.leobenkel.soteria.Modules.{Dependency, NameOfModule}
import com.leobenkel.soteria.ParentTest
import com.leobenkel.soteria.Utils.ImplicitModuleToString._
import com.leobenkel.soteria.Utils.LoggerExtended
import sbt._
import sbt.util.Level

class TaskAllDependenciesTest extends ParentTest with TaskAllDependencies {
  private val test: TaskAllDependenciesTest = this

  class LogTest extends LoggerExtended {
    private var allMessage: String = ""

    override def isSoftError: Boolean = true

    override def setSoftError(softError: Boolean): LoggerExtended =
      test.fail("Should not be called")

    final override def criticalFailure(message: => String): Unit =
      test.fail("Should not be called")

    final override def setLevel(level: Level.Value): LoggerExtended =
      test.fail("Should not be called")

    override def separator(
        level: Level.Value,
        title: String
    ): Unit = test.fail("Should not be called")

    final override def log(
        level: Level.Value,
        message: => String
    ): Unit = {
      test.log.debug(s"[$level] $message")
      allMessage += s"[$level] $message\n"
    }

    final override def trace(t: => Throwable): Unit =
      test.fail("Should not be called")

    final override def success(message: => String): Unit =
      test.fail("Should not be called")

    final def getAllMessages: String = allMessage
  }

  test("test filterLibraries - empty") {
    val inputLib = Seq()
    val dependencySearched = Dependency("com.org", "artifact")
    val log = new LogTest()
    val output = ZTestOnlyTaskAllDependencies.filterLibrariesTest(
      log = log,
      libraries = inputLib,
      whatToKeep = dependencySearched
    )

    assert(log.getAllMessages.contains(inputLib.length.toString))

    output match {
      case Left(error) =>
        error.consume { s =>
          assert(s.contains(dependencySearched.toString))

          ()
        }
      case Right(_) => fail("Should have been Left")
    }
  }

  test("test filterLibraries") {
    val version = "v1.0"
    val inputLib = Seq(
      "com.org" % "artifact" % version,
      "com.org2" % "foo" % "v2.0"
    )
    val dependencySearched = Dependency("com.org", "artifact")
    val log = new LogTest()

    val output = ZTestOnlyTaskAllDependencies.filterLibrariesTest(
      log = log,
      libraries = inputLib,
      whatToKeep = dependencySearched
    )

    assert(log.getAllMessages.contains("1")) // intersect of inputLib and dependencySearched
    assert(log.getAllMessages.contains(inputLib.length.toString))
    assert(
      log.getAllMessages
        .contains(
          dependencySearched.toModuleID(version).right.get.prettyString
        )
    )

    output match {
      case Left(_) => fail("Should have been Left")
      case Right(result) =>
        assert(result.length == 1)
        assert(dependencySearched.toModuleID(version).right.get === result.head)
    }
  }

  test("test filterLibraries - with scala") {
    val version = "v1.0"
    val inputLib = Seq(
      "com.org" % "artifact" % version,
      "com.org2" % "foo" % "v2.0",
      "org.scala-lang" % "scala" % "2.12"
    )
    val dependencySearched = Dependency("com.org", "artifact")
    val log = new LogTest()

    val output = ZTestOnlyTaskAllDependencies.filterLibrariesTest(
      log = log,
      libraries = inputLib,
      whatToKeep = dependencySearched
    )

    assert(log.getAllMessages.contains("2")) // intersect inputLib and depSearched plus scala
    assert(log.getAllMessages.contains(inputLib.length.toString))
    assert(
      log.getAllMessages
        .contains(
          dependencySearched.toModuleID(version).right.get.prettyString
        )
    )

    output match {
      case Left(_) => fail("Should have been Left")
      case Right(result) =>
        assert(result.length == 2)
        assert(
          result.exists(
            m => dependencySearched.toModuleID(version).right.get === m
          )
        )
    }
  }

  test("test getLibraryFiltered - empty") {
    val packageKnownRiskDependencies: Map[Dependency, Seq[NameOfModule]] = Map()
    val libraries: Seq[ModuleID] = Seq()
    val log = new LogTest()
    val output = ZTestOnlyTaskAllDependencies.getLibraryFilteredTest(
      log,
      packageKnownRiskDependencies,
      libraries
    )

    assert(output.isEmpty)
  }

  test("test getLibraryFiltered") {
    val packageKnownRiskDependencies: Map[Dependency, Seq[NameOfModule]] = Map(
      Dependency("com.org", "artifact") ->
        Seq(
          NameOfModule("com.org2", "foo")
        )
    )
    val libraries: Seq[ModuleID] = Seq(
      "com.org" % "artifact" % "v1.0"
    )
    val log = new LogTest()
    val output = ZTestOnlyTaskAllDependencies.getLibraryFilteredTest(
      log,
      packageKnownRiskDependencies,
      libraries
    )

    assert(output.length == 1)
    assert(output.head._2.length == 1)
    assert(log.getAllMessages.contains(libraries.head.prettyString))
    assert(log.getAllMessages.contains(output.head._2.length.toString))
  }

  test("test removeBadDependencies - remove fully") {
    val log = new LogTest()
    val needToBeReplaced: Seq[Dependency] = Seq(
      Dependency("com.org2", "foo"),
      Dependency("com.org", "artifact")
    )
    val module: ModuleID = "com.org" % "artifact" % "v1.0"
    val accumulator: Seq[ModuleID] = Seq()
    val toRemove: NameOfModule = NameOfModule("com.org2", "baz")

    val output = ZTestOnlyTaskAllDependencies.removeBadDependenciesTest(
      log,
      needToBeReplaced,
      module,
      accumulator,
      toRemove
    )

    assert(log.getAllMessages.contains("entirely"))
    assert(log.getAllMessages.contains(toRemove.toString))
    assert(log.getAllMessages.contains(module.prettyString))

    assert(output._1.exclusions.length == 1)
    assert(output._1.exclusions.head.organization == toRemove.organization)
    assert(output._1.exclusions.head.name == toRemove.name)

    assert(output._2.isEmpty)
  }

  test("test removeBadDependencies - break") {
    val log = new LogTest() {
      override def setSoftError(softError: Boolean): LoggerExtended = {
        test.assert(softError)
        this
      }
    }
    val needToBeReplaced: Seq[Dependency] = Seq(
      Dependency("com.org2", "foo"),
      Dependency("com.org", "artifact")
    )
    val module: ModuleID = "com.org" % "artifact" % "v1.0"
    val accumulator: Seq[ModuleID] = Seq()
    val toRemove: NameOfModule =
      NameOfModule("com.org2", "baz-").copy(exactName = false)

    val output = ZTestOnlyTaskAllDependencies.removeBadDependenciesTest(
      log,
      needToBeReplaced,
      module,
      accumulator,
      toRemove
    )

    assert(log.getAllMessages.contains(toRemove.toString))
    assert(log.getAllMessages.contains(module.prettyString))
    assert(log.getAllMessages.contains("Error"))

    assert(output._1.exclusions.isEmpty)
    assert(output._2.isEmpty)
  }

  test("test removeBadDependencies - replace") {
    val log = new LogTest()
    val replacement = Dependency("com.org2", "baz").withVersion("v3.0")

    val needToBeReplaced: Seq[Dependency] = Seq(
      Dependency("com.org2", "foo"),
      Dependency("com.org", "artifact"),
      replacement
    )
    val module: ModuleID = "com.org" % "artifact" % "v1.0"
    val accumulator: Seq[ModuleID] = Seq()
    val toRemove: NameOfModule = NameOfModule("com.org2", "baz")

    val output = ZTestOnlyTaskAllDependencies.removeBadDependenciesTest(
      log,
      needToBeReplaced,
      module,
      accumulator,
      toRemove
    )

    assert(log.getAllMessages.contains(replacement.toString))
    assert(log.getAllMessages.contains(toRemove.toString))
    assert(log.getAllMessages.contains(module.prettyString))

    assert(output._1.exclusions.length == 1)
    assert(output._1.exclusions.head.organization == toRemove.organization)
    assert(output._1.exclusions.head.name == toRemove.name)

    assert(output._2.length == 1)
    assert(
      output._2
        .map(_.toString)
        .contains(replacement.toModuleID.right.get.toString)
    )
    assert(
      output._2.head.prettyString == replacement.toModuleID.right.get.prettyString
    )
  }

  test("test excludeBadDependencies - empty") {
    val log = new LogTest()
    val libraryToEdit: Seq[(ModuleID, Seq[NameOfModule])] = Seq()
    val needToBeReplaced: Seq[Dependency] = Seq()
    val output = ZTestOnlyTaskAllDependencies.excludeBadDependenciesTest(
      log,
      libraryToEdit,
      needToBeReplaced
    )

    assert(log.getAllMessages.isEmpty)
    assert(output.isEmpty)
  }

  test("test excludeBadDependencies - nothing to do") {
    val log = new LogTest()
    val libraryToEdit: Seq[(ModuleID, Seq[NameOfModule])] = Seq(
      "com.org" % "artifact" % "v1.0" -> Seq()
    )
    val needToBeReplaced: Seq[Dependency] = Seq()
    val output = ZTestOnlyTaskAllDependencies.excludeBadDependenciesTest(
      log,
      libraryToEdit,
      needToBeReplaced
    )

    assert(log.getAllMessages.isEmpty)
    assert(output.length == libraryToEdit.length)
    assert(output == libraryToEdit.map(_._1))
  }

  test("test excludeBadDependencies - remove entirely") {
    val log = new LogTest()
    val libraryToEdit: Seq[(ModuleID, Seq[NameOfModule])] = Seq(
      "com.org" % "artifact" % "v1.0" ->
        Seq(
          NameOfModule("com.org2", "foo")
        )
    )
    val needToBeReplaced: Seq[Dependency] = Seq()
    val output = ZTestOnlyTaskAllDependencies.excludeBadDependenciesTest(
      log,
      libraryToEdit,
      needToBeReplaced
    )

    assert(log.getAllMessages.contains("entirely"))
    assert(
      log.getAllMessages.contains(libraryToEdit.map(_._1).head.prettyString)
    )
    assert(
      log.getAllMessages.contains(libraryToEdit.flatMap(_._2).head.toString)
    )
    assert(output.length == libraryToEdit.length)
    assert(
      output.head.prettyString == libraryToEdit.map(_._1).head.prettyString
    )
  }

  test("test excludeBadDependencies") {
    val log = new LogTest()
    val libraryToEdit: Seq[(ModuleID, Seq[NameOfModule])] = Seq(
      "com.org" % "artifact" % "v1.0" ->
        Seq(
          NameOfModule("com.org2", "foo")
        )
    )
    val needToBeReplaced: Seq[Dependency] = Seq(
      Dependency("com.org2", "foo").withVersion("v2.0")
    )
    val output = ZTestOnlyTaskAllDependencies.excludeBadDependenciesTest(
      log,
      libraryToEdit,
      needToBeReplaced
    )

    assert(
      log.getAllMessages.contains(libraryToEdit.map(_._1).head.prettyString)
    )
    assert(
      log.getAllMessages.contains(libraryToEdit.flatMap(_._2).head.toString)
    )
    assert(log.getAllMessages.contains(needToBeReplaced.head.toString))

    assert(output.length == (libraryToEdit.length + needToBeReplaced.length))
    assert(output.head.prettyString == needToBeReplaced.head.toString)
    assert(output.apply(1).prettyString == libraryToEdit.head._1.prettyString)
  }

  test("test execAllDependencies - empty") {
    val log = new LogTest() {
      override def separator(
          level: Level.Value,
          title: String
      ): Unit = {
        test.assert(
          title.contains("allDependencies") || title.contains("checkVersion") ||
            title.contains("rewriteLibraries") || title.contains(
            "TaskAllDependencies"
          )
        )

        ()
      }
    }

    val soteriaConfig: SoteriaConfiguration = SoteriaConfiguration(
      log,
      sbtVersion = "1.2.7",
      scalaVersions = Set("2.12"),
      scalaCFlags = Seq(),
      dockerImageOpt = None,
      modules = Map(
        "com.org" ->
          Map(
            "artifact" -> SerializedModule.Empty
          )
      )
    )
    val libraries: Seq[ModuleID] = Seq()
    val debugValue: Option[ModuleID] = None
    val output = ZTestOnlyTaskAllDependencies.execAllDependenciesTest(
      log,
      soteriaConfig = soteriaConfig,
      libraries = libraries,
      debugValue = debugValue
    )

    assert(log.getAllMessages.contains("allDependencies"))
    assert(log.getAllMessages.contains(libraries.length.toString))
    assert(output.isEmpty)
  }

  test("test execAllDependencies - with debug - present") {
    val log = new LogTest() {
      override def separator(
          level: Level.Value,
          title: String
      ): Unit = {
        test.assert(
          title.contains("allDependencies") || title.contains("checkVersion") ||
            title.contains("rewriteLibraries") || title.contains(
            "TaskAllDependencies"
          )
        )

        ()
      }
    }

    val soteriaConfig: SoteriaConfiguration = SoteriaConfiguration(
      log,
      sbtVersion = "1.2.7",
      scalaVersions = Set("2.12"),
      scalaCFlags = Seq(),
      dockerImageOpt = None,
      modules = Map(
        "com.org" ->
          Map(
            "artifact" -> SerializedModule.Empty
          )
      )
    )
    val libraries: Seq[ModuleID] = Seq(
      "com.org" % "artifact" % "v1.0",
      "com.org2" % "artifact2" % "v2.0"
    )
    val debugValue: Option[ModuleID] = Some("com.org" %% "artifact" % "1.0.0")
    val output = ZTestOnlyTaskAllDependencies.execAllDependenciesTest(
      log,
      soteriaConfig = soteriaConfig,
      libraries = libraries,
      debugValue = debugValue
    )

    assert(log.getAllMessages.contains("allDependencies"))
    assert(log.getAllMessages.contains(libraries.length.toString))
    assert(log.getAllMessages.contains(libraries.head.prettyString))
    assert(log.getAllMessages.contains("Found"))
    assert(log.getAllMessages.contains(debugValue.get.organization))
    assert(log.getAllMessages.contains(debugValue.get.name))

    assert(output.length == 1)
    assert(output.head.prettyString == libraries.head.prettyString)
  }

  test("test execAllDependencies - with debug - absent") {
    val log = new LogTest() {
      override def separator(
          level: Level.Value,
          title: String
      ): Unit = {
        test.assert(
          title.contains("allDependencies") || title.contains("checkVersion") ||
            title.contains("rewriteLibraries") || title.contains(
            "TaskAllDependencies"
          )
        )

        ()
      }
      override def setSoftError(softError: Boolean): LoggerExtended = {
        test.assert(softError)
        this
      }
    }

    val soteriaConfig: SoteriaConfiguration = SoteriaConfiguration(
      log,
      sbtVersion = "1.2.7",
      scalaVersions = Set("2.12"),
      scalaCFlags = Seq(),
      dockerImageOpt = None,
      modules = Map(
        "com.org" ->
          Map(
            "artifact" -> SerializedModule.Empty
          )
      )
    )
    val libraries: Seq[ModuleID] = Seq(
      "com.org" % "artifact" % "v1.0"
    )
    val debugValue: Option[ModuleID] = Some("com.org2" % "artifact2" % "0.0.0")
    val output = ZTestOnlyTaskAllDependencies.execAllDependenciesTest(
      log,
      soteriaConfig = soteriaConfig,
      libraries = libraries,
      debugValue = debugValue
    )

    assert(log.getAllMessages.contains("allDependencies"))
    assert(log.getAllMessages.contains(libraries.length.toString))
    assert(log.getAllMessages.contains(libraries.head.prettyString))
    assert(log.getAllMessages.contains("Could not find"))
    assert(log.getAllMessages.contains(debugValue.get.organization))
    assert(log.getAllMessages.contains(debugValue.get.name))

    assert(output.isEmpty)
  }

  test("test execAllDependencies") {
    val log = new LogTest() {
      override def separator(
          level: Level.Value,
          title: String
      ): Unit = {
        test.assert(
          title.contains("allDependencies") || title.contains("checkVersion") ||
            title.contains("rewriteLibraries") || title.contains(
            "TaskAllDependencies"
          )
        )

        ()
      }
    }

    val soteriaConfig: SoteriaConfiguration = SoteriaConfiguration(
      log,
      sbtVersion = "1.2.7",
      scalaVersions = Set("2.12"),
      scalaCFlags = Seq(),
      dockerImageOpt = None,
      modules = Map(
        "com.org" ->
          Map(
            "artifact" -> SerializedModule.Empty
          )
      )
    )
    val libraries: Seq[ModuleID] = Seq(
      "com.org" % "artifact" % "v1.0",
      "com.org2" % "artifact2" % "v2.0"
    )
    val debugValue: Option[ModuleID] = None
    val output = ZTestOnlyTaskAllDependencies.execAllDependenciesTest(
      log,
      soteriaConfig = soteriaConfig,
      libraries = libraries,
      debugValue = debugValue
    )

    assert(log.getAllMessages.contains("allDependencies"))
    assert(log.getAllMessages.contains(libraries.length.toString))
    assert(log.getAllMessages.contains(libraries.head.prettyString))

    assert(output.length == libraries.length)
    assert(output.head.prettyString == libraries.head.prettyString)
  }

  test("test rewriteLibAndVersionCheck - wrong version") {
    val log = new LogTest() {
      override def separator(
          level: Level.Value,
          title: String
      ): Unit = {
        test.assert(
          title.contains("checkVersion") || title.contains(
            "TaskAllDependencies.rewriteLibraries"
          )
        )

        ()
      }

      override def isSoftError: Boolean = false
    }

    val soteriaConfig: SoteriaConfiguration = SoteriaConfiguration(
      log,
      sbtVersion = "1.2.7",
      scalaVersions = Set("2.12"),
      scalaCFlags = Seq(),
      dockerImageOpt = None,
      modules = Map(
        "com.org" ->
          Map(
            "artifact" -> SerializedModule.Empty.copy(version = "v4.0")
          )
      )
    )
    val libraries: Seq[ModuleID] = Seq(
      "com.org" % "artifact" % "v1.0"
    )

    val output = ZTestOnlyTaskAllDependencies.rewriteLibAndVersionCheckTest(
      log,
      soteriaConfig,
      libraries
    )

    libraries.foreach { m =>
      assert(log.getAllMessages.contains(m.prettyString))

      ()
    }

    assert(log.getAllMessages.contains(libraries.length.toString))

    output match {
      case Right(_) =>
        fail("Should have failed")
      case Left(error) =>
        error.consume { s =>
          assert(s.contains(libraries.head.prettyString))
          assert(s.contains(soteriaConfig.AllModules.head.toString))
          ()
        }
    }
  }

  test("test rewriteLibAndVersionCheck") {
    val log = new LogTest() {
      override def separator(
          level: Level.Value,
          title: String
      ): Unit = {
        test.assert(
          title.contains("allDependencies") || title.contains("checkVersion") ||
            title.contains("TaskAllDependencies.rewriteLibraries")
        )

        ()
      }
    }

    val soteriaConfig: SoteriaConfiguration = SoteriaConfiguration(
      log,
      sbtVersion = "1.2.7",
      scalaVersions = Set("2.12"),
      scalaCFlags = Seq(),
      dockerImageOpt = None,
      modules = Map(
        "com.org" ->
          Map(
            "artifact" -> SerializedModule.Empty
          )
      )
    )
    val libraries: Seq[ModuleID] = Seq(
      "com.org" % "artifact" % "v1.0",
      "com.org2" % "artifact2" % "v2.0"
    )

    val output = ZTestOnlyTaskAllDependencies.rewriteLibAndVersionCheckTest(
      log,
      soteriaConfig,
      libraries
    )

    libraries.foreach { m =>
      assert(log.getAllMessages.contains(m.prettyString))

      ()
    }

    assert(log.getAllMessages.contains(libraries.length.toString))

    output match {
      case Right(modules) =>
        assert(modules.length == libraries.length)
      case Left(_) => fail("Should not have failed")
    }
  }

  test("test rewriteLibraries") {
    val log = new LogTest() {
      override def separator(
          level: Level.Value,
          title: String
      ): Unit = {
        test.assert(
          title.contains("allDependencies") || title.contains("checkVersion") ||
            title.contains("TaskAllDependencies.rewriteLibraries")
        )

        ()
      }
    }

    val soteriaConfig: SoteriaConfiguration = SoteriaConfiguration(
      log,
      sbtVersion = "1.2.7",
      scalaVersions = Set("2.12"),
      scalaCFlags = Seq(),
      dockerImageOpt = None,
      modules = Map(
        "com.org" ->
          Map(
            "artifact" -> SerializedModule.Empty
          )
      )
    )
    val libraries: Seq[ModuleID] = Seq(
      "com.org" % "artifact" % "v1.0",
      "com.org2" % "artifact2" % "v2.0"
    )

    val output = ZTestOnlyTaskAllDependencies.rewriteLibrariesTest(
      log,
      soteriaConfig,
      libraries
    )

    libraries.foreach { m =>
      assert(log.getAllMessages.contains(m.prettyString))

      ()
    }

    assert(log.getAllMessages.contains(libraries.length.toString))

    assert(output.length == libraries.length)
  }
}
