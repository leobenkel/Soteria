package com.leobenkel.soteria.Transformations

import com.leobenkel.soteria.Config.SoteriaConfiguration
import com.leobenkel.soteria.Modules.Dependency
import com.leobenkel.soteria.SoteriaPluginKeys
import com.leobenkel.soteria.SoteriaPluginKeys.soteriaGetLog
import com.leobenkel.soteria.Utils.ImplicitModuleToString._
import com.leobenkel.soteria.Utils.LoggerExtended
import sbt.{Configuration, Def, Keys, ModuleID}

private[Transformations] trait TaskLibraryDependencies {

  private def debugLibraryDependencies(
    log:                 LoggerExtended,
    conf:                Option[Configuration],
    libraryDependencies: Seq[ModuleID]
  ): Unit = {
    log.separatorDebug(s"$conf / libraryDependencies")
    log.debug(
      s"Found ${libraryDependencies.size} libraries in 'libraryDependencies': "
    )
    libraryDependencies.prettyString(log, s"$conf/libraryDependencies")
  }

  private def getForbiddenVersionErrors(
    libraryDependencies: Seq[ModuleID],
    forbiddenModules:    Seq[(Dependency, String)]
  ): Seq[String] =
    for {
      inputLib     <- libraryDependencies
      forbidModule <- forbiddenModules if forbidModule._1 === inputLib
    } yield s"${inputLib.prettyString}\n   Detailed error > ${forbidModule._2}"

  private def getProvidedEnforcedErrors(
    libraryDependencies: Seq[ModuleID],
    mustBeProvided:      Seq[Dependency]
  ): Seq[String] =
    for {
      providedLib <- mustBeProvided
      inputLib <- libraryDependencies
        .filterNot(_.configurations.getOrElse("").contains("test"))
        .filterNot(_.configurations.getOrElse("").contains("provided"))
      if providedLib === inputLib
    } yield s"${inputLib.prettyString}\n   Detailed error > Should be marked as provided."

  private def processLibraryDependencies(
    log:                 LoggerExtended,
    config:              SoteriaConfiguration,
    conf:                Option[Configuration],
    libraryDependencies: Seq[ModuleID]
  ): Seq[sbt.ModuleID] = {
    debugLibraryDependencies(log, conf, libraryDependencies)

    val errors =
      (getForbiddenVersionErrors(
        libraryDependencies,
        config.ForbiddenModules
      ) ++ getProvidedEnforcedErrors(libraryDependencies, config.AsProvided)).sortBy(identity)

    if (errors.nonEmpty)
      log.fail(
        s"You have errors in your 'libraryDependencies': \n${errors.mkString("\n")}"
      )

    libraryDependencies
  }

  def libraryDependencies(
    conf: Option[Configuration]
  ): Def.Initialize[Seq[ModuleID]] =
    Def.settingDyn {
      val log = soteriaGetLog.value
      val libraryDependencies =
        conf.fold(Keys.libraryDependencies)(_ / Keys.libraryDependencies).value
      val config = SoteriaPluginKeys.soteriaConfig.value

      Def.setting {
        processLibraryDependencies(log, config, conf, libraryDependencies)
      }
    }
}
