package com.leobenkel.soteria.Transformations

import com.leobenkel.soteria.Messages.{ErrorMessage, NoError}
import com.leobenkel.soteria.Modules.Dependency
import com.leobenkel.soteria.ParentTest
import com.leobenkel.soteria.Utils.LoggerExtended
import sbt._
import sbt.util.Level

// scalastyle:off magic.number
class CheckVersionTest extends ParentTest with CheckVersion {
  private val test: CheckVersionTest = this

  private abstract class LogTest(test: CheckVersionTest) extends LoggerExtended {
    override def isSoftError: Boolean = false

    override def criticalFailure(message: => String): Unit = test.fail("Should not be called")

    override def setSoftError(softError: Boolean): LoggerExtended =
      test.fail("Should not be called")

    override def setLevel(level: Level.Value): LoggerExtended = test.fail("Should not be called")

    override def trace(t: => Throwable): Unit = test.fail("Should not be called")

    override def success(message: => String): Unit = test.fail("Should not be called")
  }

  test("Test checkVersion - do nothing") {
    val libraryInput = Seq[ModuleID]()

    val result =
      checkVersion(
        log =
          new LogTest(test) {
            override def separator(
                level: Level.Value,
                title: String,
            ): Unit = {
              test.assert(title.contains("checkVersion"))

              ()
            }

            override def log(
                level:   Level.Value,
                message: => String,
            ): Unit = {
              test.assert(message.contains(libraryInput.length.toString))

              ()
            }
          },
        allCorrectLibraries = Seq(),
        libraries = libraryInput,
        moreErrors = NoError,
      )

    assert(result.isRight)
  }

  test("Test checkVersion - keep errors") {
    val libraryInput = Seq[ModuleID]()

    val previousTitleError   = "Error Title 1"
    val previousMessageError = "Message error"
    val error                =
      ErrorMessage(
        previousTitleError,
        previousMessageError,
      )
    val result               =
      checkVersion(
        log =
          new LogTest(test) {
            override def separator(
                level: Level.Value,
                title: String,
            ): Unit = {
              test.assert(title.contains("checkVersion"))

              ()
            }

            override def log(
                level:   Level.Value,
                message: => String,
            ): Unit = {
              test.assert(message.contains(libraryInput.length.toString))

              ()
            }
          },
        allCorrectLibraries = Seq(),
        libraries = libraryInput,
        moreErrors = error,
      )

    assert(result.isLeft)
    val errors = result.left.get
    assert(errors.errorMessage.contains(error))
  }

  test("Test checkVersion - no errors") {
    val libraryInput =
      Seq[ModuleID](
        "com.org" % "artifactName" % "v9.0"
      )
    val knowledge    = libraryInput.map(m => Dependency.apply(m))

    var countLog = 0

    val result =
      checkVersion(
        log =
          new LogTest(test) {
            override def separator(
                level: Level.Value,
                title: String,
            ): Unit = {
              test.assert(title.contains("checkVersion"))

              ()
            }

            override def log(
                level:   Level.Value,
                message: => String,
            ): Unit = {
              countLog match {
                case 0 => test.assert(message.contains(libraryInput.length.toString))
                case n =>
                  val libIndex = n - 1
                  test.assert(libIndex < libraryInput.length)
                  test.assert(message.contains(libraryInput.apply(libIndex).name))
                  test.assert(
                    message.contains(libraryInput.apply(libIndex).organization)
                  )
              }
              countLog += 1
            }
          },
        allCorrectLibraries = knowledge,
        libraries = libraryInput,
        moreErrors = NoError,
      )

    assert(result.isRight)
    result
      .right
      .get
      .consume { s =>
        assert(s.contains(libraryInput.length.toString))
        test.log.debug(s)
      }
  }

  test("Test checkVersion - no full overlap - more input Lib") {
    val libraryInput =
      Seq[ModuleID](
        "com.org"  % "artifactName1" % "v8.0",
        "com.org2" % "artifactName3" % "v6.0",
        "com.org"  % "artifactName2" % "v7.0",
      )
    val knowledge    = libraryInput.take(2).map(m => Dependency.apply(m))

    var countLog = 0

    val result =
      checkVersion(
        log =
          new LogTest(test) {
            override def separator(
                level: Level.Value,
                title: String,
            ): Unit = {
              test.assert(title.contains("checkVersion"))

              ()
            }

            override def log(
                level:   Level.Value,
                message: => String,
            ): Unit = {
              countLog match {
                case 0 => test.assert(message.contains(knowledge.length.toString))
                case n =>
                  val libIndex = n - 1
                  test.assert(libIndex < libraryInput.length)
                  test.assert(message.contains(libraryInput.apply(libIndex).name))
                  test.assert(
                    message.contains(libraryInput.apply(libIndex).organization)
                  )
              }
              countLog += 1
            }
          },
        allCorrectLibraries = knowledge,
        libraries = libraryInput,
        moreErrors = NoError,
      )

    assert(result.isRight)
    result
      .right
      .get
      .consume { s =>
        assert(s.contains(knowledge.length.toString))
        log.debug(s)
      }
  }

  test("Test checkVersion - no full overlap - more knowledge") {
    val libraryInput =
      Seq[ModuleID](
        "com.org"  % "artifactName1" % "v8.0",
        "com.org2" % "artifactName3" % "v6.0",
        "com.org"  % "artifactName2" % "v7.0",
        "com.org3" % "artifactName5" % "v0.0",
      )
    val knowledge    =
      libraryInput.take(2).map(m => Dependency.apply(m)) ++
        Seq(
          Dependency("com.org3" % "artifactName4" % "v9.0"),
          Dependency("com.org4" % "artifactName6" % "v88.0"),
          Dependency("com.org4" % "artifactName7" % "v99.0"),
        )

    val overlap = knowledge.map(_.key) intersect libraryInput.map(m => (m.organization, m.name))

    var countLog = 0

    val result =
      checkVersion(
        log =
          new LogTest(test) {
            override def separator(
                level: Level.Value,
                title: String,
            ): Unit = {
              test.assert(title.contains("checkVersion"))

              ()
            }

            override def log(
                level:   Level.Value,
                message: => String,
            ): Unit = {
              countLog match {
                case 0 => test.assert(message.contains(overlap.length.toString))
                case n =>
                  val libIndex = n - 1
                  test.assert(libIndex < libraryInput.length)
                  test.assert(message.contains(libraryInput.apply(libIndex).name))
                  test.assert(
                    message.contains(libraryInput.apply(libIndex).organization)
                  )
              }
              countLog += 1
            }
          },
        allCorrectLibraries = knowledge,
        libraries = libraryInput,
        moreErrors = NoError,
      )

    assert(result.isRight)
    result
      .right
      .get
      .consume { s =>
        assert(s.contains(overlap.length.toString))

        ()
      }
  }

  test("Test checkVersion - not exact match") {
    val libraryInput =
      Seq[ModuleID](
        "com.org"  % "artifactName1" % "v5.0",
        "com.org"  % "artifactName3" % "v5.0",
        "com.org"  % "artifactName2" % "v5.0",
        "com.org3" % "artifactName5" % "v0.0",
      )

    val knowledge =
      Seq(
        Dependency("com.org", "art").withName(_.copy(exactName = false)).withVersion("v5.0")
      )

    val overlap =
      libraryInput.filter(m => knowledge.exists(_ === m)).sortBy(m => (m.organization, m.name))

    var countLog = 0

    val result =
      checkVersion(
        log =
          new LogTest(test) {
            override def separator(
                level: Level.Value,
                title: String,
            ): Unit = {
              test.assert(title.contains("checkVersion"))

              ()
            }

            override def log(
                level:   Level.Value,
                message: => String,
            ): Unit = {
              countLog match {
                case 0 => test.assert(message.contains(overlap.length.toString))
                case n =>
                  val libIndex = n - 1
                  test.assert(libIndex < overlap.length)
                  test.assert(message.contains(overlap.apply(libIndex).name))
                  test.assert(
                    message.contains(overlap.apply(libIndex).organization)
                  )
              }
              countLog += 1
            }
          },
        allCorrectLibraries = knowledge,
        libraries = libraryInput,
        moreErrors = NoError,
      )

    assert(result.isRight)
    result
      .right
      .get
      .consume { s =>
        assert(s.contains(overlap.length.toString))

        ()
      }
  }

  test("Test checkVersion - not exact match - with errors") {
    val badDependency = "com.org" % "artifactName3" % "v7.0"
    val goodVersion   = "v5.0"
    val libraryInput  =
      Seq[ModuleID](
        "com.org"  % "artifactName1" % goodVersion,
        badDependency,
        "com.org"  % "artifactName2" % goodVersion,
        "com.org3" % "artifactName5" % "v0.0",
      )

    val knowledge =
      Seq(
        Dependency("com.org", "art").withName(_.copy(exactName = false)).withVersion(goodVersion)
      )

    val overlap =
      libraryInput.filter(m => knowledge.exists(_ === m)).sortBy(m => (m.organization, m.name))

    var countLog = 0

    val result =
      checkVersion(
        log =
          new LogTest(test) {
            override def separator(
                level: Level.Value,
                title: String,
            ): Unit = {
              test.assert(title.contains("checkVersion"))

              ()
            }

            override def log(
                level:   Level.Value,
                message: => String,
            ): Unit = {
              countLog match {
                case 0 => test.assert(message.contains(overlap.length.toString))
                case n =>
                  val libIndex = n - 1
                  test.assert(libIndex < overlap.length)
                  test.assert(message.contains(overlap.apply(libIndex).name))
                  test.assert(
                    message.contains(overlap.apply(libIndex).organization)
                  )
              }
              countLog += 1
            }
          },
        allCorrectLibraries = knowledge,
        libraries = libraryInput,
        moreErrors = NoError,
      )

    assert(result.isLeft)
    result
      .left
      .get
      .consume { s =>
        assert(s.contains("Wrong versions"))
        assert(s.contains(badDependency.name))
        assert(s.contains(badDependency.organization))
        assert(s.contains(badDependency.revision))
        assert(s.contains(goodVersion))

        ()
      }
  }

  test("Test getLibraryToCheck - Empty") {
    val knowledge = Seq[Dependency]()
    val inputLib  = Seq[ModuleID]()
    val output    =
      ZTestOnlyCheckVersion.getLibraryToCheckTest(
        knowledge,
        inputLib,
      )

    assertEquals(0, output.length)
  }

  test("Test getLibraryToCheck") {
    val knowledge =
      Seq[Dependency](
        Dependency("com.org", "art").withName(_.copy(exactName = false)),
        Dependency("com.org2", "bar"),
      )
    val inputLib  =
      Seq[ModuleID](
        "com.org"  % "artifact"  % "v1.0",
        "com.org"  % "artifact2" % "v1.0",
        "com.org"  % "foo"       % "v1.0",
        "com.org2" % "bar"       % "v5.0",
        "com.org2" % "barfoo"    % "v3.0",
      )

    val output =
      ZTestOnlyCheckVersion.getLibraryToCheckTest(
        knowledge,
        inputLib,
      )

    assertEquals(3, output.length)
  }

  test("Test buildError - empty") {
    val inputLib       = Seq[ModuleID]()
    val knowledge      = Dependency("com.org", "art").withName(_.copy(exactName = false))
    val correctVersion = "v1.0"
    val output         =
      ZTestOnlyCheckVersion.buildErrorsTest(
        inputLib,
        knowledge,
        correctVersion,
      )

    assert(output.isEmpty)
  }

  test("Test buildError - no error") {
    val inputLib       =
      Seq[ModuleID](
        "com.org"  % "artifact"  % "v1.0",
        "com.org"  % "artifact2" % "v1.0",
        "com.org"  % "foo"       % "v1.0",
        "com.org2" % "bar"       % "v5.0",
        "com.org2" % "barfoo"    % "v3.0",
      )
    val knowledge      = Dependency("com.org", "art").withName(_.copy(exactName = false))
    val correctVersion = "v1.0"
    val output         =
      ZTestOnlyCheckVersion.buildErrorsTest(
        inputLib,
        knowledge,
        correctVersion,
      )

    assert(output.isEmpty)
  }

  test("Test buildError - with error") {
    val badLib         = "com.org" % "artifact2" % "v2.0"
    val inputLib       =
      Seq[ModuleID](
        badLib,
        "com.org"  % "artifact" % "v1.0",
        "com.org"  % "foo"      % "v1.0",
        "com.org2" % "bar"      % "v5.0",
        "com.org2" % "barfoo"   % "v3.0",
      )
    val knowledge      = Dependency("com.org", "art").withName(_.copy(exactName = false))
    val correctVersion = "v1.0"
    val output         =
      ZTestOnlyCheckVersion.buildErrorsTest(
        inputLib,
        knowledge,
        correctVersion,
      )

    assertEquals(1, output.length)
    val error = output.head
    assert(error.contains(correctVersion))
    assert(error.contains(badLib.name))
    assert(error.contains(badLib.organization))
  }
}
// scalastyle:on magic.number
