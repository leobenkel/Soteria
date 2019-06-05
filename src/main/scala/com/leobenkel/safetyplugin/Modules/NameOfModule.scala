package com.leobenkel.safetyplugin.Modules

import com.leobenkel.safetyplugin.Config.SerializedModule
import sbt.librarymanagement.ModuleID
import sbt.{ExclusionRule, _}

object NameOfModule {
  def apply(module: ModuleID): NameOfModule = {
    NameOfModule(
      module.organization,
      module.name,
      exactName = ModuleDefaults.ExactName,
      excludeName = Seq.empty,
      needDoublePercent = ModuleDefaults.NeedDoublePercent
    )
  }

  def apply(
    org:  String,
    name: String
  ): NameOfModule = {
    NameOfModule(
      org,
      name,
      exactName = ModuleDefaults.ExactName,
      excludeName = Seq.empty,
      needDoublePercent = ModuleDefaults.NeedDoublePercent
    )
  }

  def find(
    data: Map[String, Map[String, SerializedModule]]
  )(
    s: String
  ): Either[String, NameOfModule] = {
    fromPath(s).flatMap {
      case (org, name) =>
        for {
          inOrg <- data.get(org) match {
            case None    => Left(s"Could not find org: '$org' in available knowledge")
            case Some(m) => Right(m)
          }
          module <- inOrg.get(name) match {
            case None =>
              Left(
                s"Could not find name: '$name' " +
                  s"in available knowledge for org '$org'"
              )
            case Some(m) => Right(m)
          }
        } yield {
          module.toNameOfModule(org, name)
        }
    }
  }

  private def fromPath(s: String): Either[String, (String, String)] = {
    val pieces = s.split('|').map(_.trim)
    if (pieces.length != 2) {
      Left(s"Was not able to get module with dependency override name being: '$s'")
    } else {
      val org = pieces(0)
      val name = pieces(1)
      Right((org, name))
    }
  }
}

case class NameOfModule(
  organization:      String,
  name:              String,
  exactName:         Boolean,
  excludeName:       Seq[String],
  needDoublePercent: Boolean
) {
  @transient lazy val key:                      (String, String) = (organization, name)
  @transient lazy private val percentConnector: String = if (needDoublePercent) "%%" else "%"
  @transient lazy private val lowerCaseName:    String = name.toLowerCase
  @transient lazy val toPath:                   String = s"$organization | $name"
  @transient lazy override val toString: String =
    s""" "$organization" $percentConnector "$name" """.trim

  def nameMatch(otherName: String): Boolean = {
    if (exactName) {
      otherName.toLowerCase == lowerCaseName
    } else {
      isExcludedName(otherName) && otherName.toLowerCase.startsWith(lowerCaseName)
    }
  }

  private def isExcludedName(testName: String): Boolean = !excludeName.exists(testName.startsWith)

  @transient lazy val toOrganizationArtifactName: Either[String, Dependency.OrgArtifact] = {
    if (exactName) {
      if (needDoublePercent) {
        Right(organization %% name)
      } else {
        Right(organization % name)
      }
    } else {
      Left(
        s"${this.toString}: The name was not exact ($exactName), " +
          s"could not create a moduleID for name: '$name'."
      )
    }
  }

  @transient lazy val exclusionRule: Either[String, sbt.ExclusionRule] = {
    if (exactName) {
      Right(ExclusionRule(organization = this.organization, name = this.name))
    } else {
      Left(
        s"${this.toString}: The name was not exact ($exactName), " +
          s"could not create an exclusion rule for name: '$name'."
      )
    }
  }

  def toModuleID(revision: String): Either[String, ModuleID] = {
    toOrganizationArtifactName.right.map(_ % revision)
  }

  def ===(other: NameOfModule): Boolean = {
    this.organization == other.organization &&
    nameMatch(other.name)
  }
}
