package com.leobenkel.soteria.Transformations

import com.leobenkel.soteria.Messages.CommonMessage._
import com.leobenkel.soteria.Messages.ErrorMessage
import com.leobenkel.soteria.Modules.Dependency
import com.leobenkel.soteria.Utils.ImplicitModuleToString._
import com.leobenkel.soteria.Utils.LoggerExtended
import sbt.ModuleID

private[Transformations] trait CheckVersion {

  /**
   * This will check that the libraries have the legal versions.
   *
   * @param libraries
   *   the libraries fetch by the build, coming from [[sbt.Keys.allDependencies]]
   * @param log
   *   The current log
   */
  def checkVersion(
    log:                 LoggerExtended,
    allCorrectLibraries: Seq[Dependency],
    libraries:           Seq[ModuleID],
    moreErrors:          ErrorMessage = ErrorMessage.Empty
  ): ResultMessages = {
    log.separatorDebug("checkVersion")

    val librariesToCheck: Seq[ModuleID] =
      getLibraryToCheck(allCorrectLibraries, libraries)

    // librariesToCheck.size == (allCorrectLibraries intersect libraries).size
    log.debug(s"> Verifying version of libraries (${librariesToCheck.size}) :")
    librariesToCheck.prettyString(log, "checkVersion")

    consolidateErrors(allCorrectLibraries, librariesToCheck, moreErrors)
  }

  private def getLibraryToCheck(
    allCorrectLibraries: Seq[Dependency],
    libraries:           Seq[ModuleID]
  ): Seq[ModuleID] =
    (for {
      correctLibrary <- allCorrectLibraries
      library        <- libraries if correctLibrary === library
    } yield library).sortBy(m => (m.organization, m.name))

  private def consolidateErrors(
    allCorrectLibraries: Seq[Dependency],
    librariesToCheck:    Seq[ModuleID],
    moreErrors:          ErrorMessage
  ): ResultMessages =
    (allCorrectLibraries
      .filter(_.version.isRight)
      .map(m => (m, m.version.right.get))
      .flatMap {
        case (correctModule, correctVersion) =>
          buildErrors(librariesToCheck, correctModule, correctVersion)
      }
      .toError("Wrong versions") ++ moreErrors).resolve(
      s"> All ${librariesToCheck.size} libraries that we " + s"know have the right versions"
    )

  private def buildErrors(
    librariesToCheck: Seq[ModuleID],
    correctModule:    Dependency,
    correctVersion:   String
  ): Seq[String] =
    librariesToCheck
      .filter(m => (correctModule === m) && m.revision != correctVersion)
      .map { m =>
        val correctModuleToString = m.withRevision(correctVersion).withName(m.name).prettyString

        s"${m.prettyString} should be $correctModuleToString"
      }

  object ZTestOnlyCheckVersion {
    @inline def buildErrorsTest(
      librariesToCheck: Seq[ModuleID],
      correctModule:    Dependency,
      correctVersion:   String
    ): Seq[String] = buildErrors(librariesToCheck, correctModule, correctVersion)

    @inline def getLibraryToCheckTest(
      allCorrectLibraries: Seq[Dependency],
      libraries:           Seq[ModuleID]
    ): Seq[ModuleID] = getLibraryToCheck(allCorrectLibraries, libraries)
  }
}
