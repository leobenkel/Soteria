package com.leobenkel.soteria.Utils

import com.leobenkel.soteria.Modules.Dependency
import sbt.librarymanagement.ModuleID

private[soteria] object ImplicitModuleToString {

  implicit class ModuleToString(m: ModuleID) {
    final def prettyString: String =
      s"""["${m.organization}" % "${m.name}" % "${m.revision}"]"""
  }

  implicit class ModuleToStringSeq(mm: Seq[ModuleID]) {
    final def prettyString(
        log: LoggerExtended,
        header: String
    ): Unit = {
      def toKey(m: ModuleID): (String, String, String) = {
        (m.organization, m.name, m.revision)
      }

      mm.groupBy(toKey)
        .flatMap { case (_, modules) => modules.headOption }
        .toSeq
        .sortBy(toKey)
        .foreach(m => log.debug(s"[$header] ${m.prettyString}"))
    }
  }

  implicit class SoteriaModuleToStringSeq(mm: Seq[Dependency]) {
    final def prettyString(
        log: LoggerExtended,
        header: String
    ): Unit = {
      def toKey(m: Dependency): (String, String, Option[String]) = {
        (m.organization, m.name, m.versions.headOption)
      }

      mm.groupBy(toKey)
        .flatMap { case (_, modules) => modules.headOption }
        .toSeq
        .sortBy(toKey)
        .foreach(m => log.debug(s"[$header] ${m.toString}"))
    }
  }
}
