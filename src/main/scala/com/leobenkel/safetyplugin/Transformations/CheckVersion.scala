package com.leobenkel.safetyplugin.Transformations

import com.leobenkel.safetyplugin.Config.SafetyConfiguration
import com.leobenkel.safetyplugin.Messages.CommonMessage.ResultMessages
import com.leobenkel.safetyplugin.Messages.ErrorMessage
import com.leobenkel.safetyplugin.Utils.LoggerExtended
import com.leobenkel.safetyplugin.Utils.ImplicitModuleToString._
import com.leobenkel.safetyplugin.Messages.CommonMessage._
import sbt.ModuleID

private[Transformations] trait CheckVersion {

  /**
    * This will check that the libraries have the legal versions.
    *
    * @param libraries the libraries fetch by the build, coming from [[sbt.Keys.allDependencies]]
    * @param log       The current log
    */
   def checkVersion(
    log:        LoggerExtended,
    config: SafetyConfiguration,
    libraries:  Seq[ModuleID],
    moreErrors: ErrorMessage = ErrorMessage.Empty
  ): ResultMessages = {
    log.separatorDebug("LibraryDependencyWriter.checkVersion")

    val allCorrectLibraries = config.CorrectVersions

    val librariesToCheck: Seq[ModuleID] = for {
      correctLibrary <- allCorrectLibraries
      library        <- libraries if correctLibrary === library
    } yield {
      library
    }

    log.debug(s"> Verifying version of libraries (${librariesToCheck.size}) :")
    librariesToCheck.prettyString(log, "checkVersion")

    (allCorrectLibraries
      .filter(_.version.isRight)
      .map(m => (m, m.version.right.get))
      .flatMap {
        case (correctModule, correctVersion) =>
          librariesToCheck
            .filter(m => (correctModule === m) && m.revision != correctVersion)
            .map { m =>
              val correctModuleToString = m
                .withRevision(correctVersion)
                .withName(correctModule.name)
                .prettyString

              s"${m.prettyString} should be $correctModuleToString"
            }
      }
      .toError("Wrong versions") ++ moreErrors)
      .resolve(
        s"> All ${librariesToCheck.size} libraries that we " +
          s"know have the right versions"
      )
  }
}
