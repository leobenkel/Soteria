package com.leobenkel.safetyplugin.Modules

import com.leobenkel.safetyplugin.Config.SerializedModule
import com.leobenkel.safetyplugin.Modules.ScalaVersionHandler._
import sbt._
import sbt.librarymanagement.{DependencyBuilders, ModuleID}

case class Dependency(
  nameObj:              NameOfModule,
  shouldDownload:       Boolean,
  versions:             Set[String],
  overrideIsEnough:     Boolean,
  forbidden:            Option[String],
  dependenciesToRemove: Seq[NameOfModule],
  shouldBeProvided:     Boolean = false,
  scalaVersionsFilter:  Seq[ScalaVersionHandler]
) {
  @transient lazy val name:                 String = nameObj.name
  @transient lazy val organization:         String = nameObj.organization
  @transient lazy val isForbidden:          Boolean = forbidden.isDefined
  @transient lazy val forbiddenExplanation: String = forbidden.get
  @transient lazy val versionKey: String = if (versions.isEmpty) {
    SerializedModule.DefaultVersionString
  } else {
    versions.mkString(" | ")
  }
  @transient lazy val version: Either[String, String] = if (versions.size == 1) {
    Right(versions.head)
  } else {
    Left(s"Expect to have one version but had ${versions.size}")
  }
  @transient lazy val tooManyVersions:   Boolean = versions.size > 1
  @transient lazy val key:               (String, String) = nameObj.key
  @transient lazy override val toString: String = s"[$jsonKey]"
  @transient lazy val needToBeReplaced:  Boolean = !overrideIsEnough && exclusionRule.isRight
  @transient lazy val isCorrectVersion:  Boolean = version.isRight
  @transient lazy val exclusionRule:     Either[String, ExclusionRule] = nameObj.exclusionRule
  @transient lazy val toOrganizationArtifactName: Either[String, Dependency.OrgArtifact] =
    nameObj.toOrganizationArtifactName
  @transient lazy val jsonKey: String = if (versions.nonEmpty) {
    s"$nameObj % ${versions.map(v => s""" "$v" """.trim).mkString(" | ")}"
  } else {
    s"$nameObj"
  }
  @transient lazy val toSerializedModule: SerializedModule = SerializedModule(
    version = version.fold(_ => SerializedModule.DefaultVersionString, identity),
    exactName = ModuleDefaults
      .toOptionWithDefault(ModuleDefaults.ExactName, nameObj.exactName),
    excludeName = ModuleDefaults.toOption(nameObj.excludeName),
    needDoublePercent = ModuleDefaults
      .toOptionWithDefault(ModuleDefaults.NeedDoublePercent, nameObj.needDoublePercent),
    shouldDownload = ModuleDefaults
      .toOptionWithDefault(ModuleDefaults.ShouldDownload, this.shouldDownload),
    overrideIsEnough = ModuleDefaults
      .toOptionWithDefault(ModuleDefaults.OverrideIsEnough, this.overrideIsEnough),
    forbidden = this.forbidden,
    shouldBeProvided = ModuleDefaults
      .toOptionWithDefault(ModuleDefaults.ShouldBeProvided, this.shouldBeProvided),
    dependenciesToRemove = ModuleDefaults.toOption(this.dependenciesToRemove.map(_.toPath).sorted),
    scalaVersionsFilter = ModuleDefaults.toOption(this.scalaVersionsFilter.map(_.serialized))
  )
  @transient lazy val toConfig: Map[String, Map[String, SerializedModule]] = Map(
    this.organization -> Map(
      this.name -> this.toSerializedModule
    )
  )

  @transient lazy val toModuleID: Either[String, ModuleID] = version.flatMap(nameObj.toModuleID)
  def toModuleID(revision: String):     Either[String, ModuleID] = nameObj.toModuleID(revision)
  def ===(other:           Dependency): Boolean = this.nameObj === other.nameObj
  def =!=(other:           Dependency): Boolean = !(this === other)
  def ===(other:           ModuleID):   Boolean = this === Dependency(other)
  def =!=(other:           ModuleID):   Boolean = !(this === Dependency(other))

  def |+|(other: Dependency): Either[String, Dependency] = {
    if (this === other) {
      Right(this.copy(versions = this.versions ++ other.versions))
    } else {
      Left(
        s"${this.toString}: Could not combine the versions since the names were different: " +
          s"1: ${this.toString} | 2: ${other.toString}"
      )
    }
  }
  def |+|(other: Option[Dependency]): Either[String, Dependency] = {
    other
      .map(this |+| _)
      .getOrElse(Right(this))
  }

  def withName(f: NameOfModule => NameOfModule): Dependency = this.copy(nameObj = f(this.nameObj))

  def withVersion(version: String): Dependency = this.copy(versions = Set(version))

  private def shouldBeTestedForInclusion(m: ModuleID): Boolean = {
    ((m.name.contains("_") || m.crossVersion.isInstanceOf[CrossVersion.Binary]) ||
    this.nameObj.needDoublePercent || this.scalaVersionsFilter.nonEmpty) && this.shouldDownload
  }

  def shouldBeDownloaded(scalaV: ScalaV): Boolean = {
    this.toModuleID.right.map(shouldBeDownloaded(scalaV, _)).right.getOrElse(false)
  }

  def shouldBeDownloaded(
    scalaV: ScalaV,
    m:      ModuleID
  ): Boolean = {
    if (shouldBeTestedForInclusion(m)) {
      val isRightScalaVersion = this.scalaVersionsFilter.applyTo(scalaV)
      if (isRightScalaVersion) {
        val m2 = scalaV.crossVersion(m)
        if (m2.name.contains("_")) {
          val nameBlocks = m2.name.split("_")
          val moduleScalaVersion = nameBlocks.last
          scalaV === moduleScalaVersion
        } else {
          true
        }
      } else {
        false
      }
    } else {
      this.shouldDownload
    }
  }
}

object Dependency {
  type OrgArtifact = DependencyBuilders.OrganizationArtifactName

  def apply(module: ModuleID): Dependency = {
    Dependency(
      nameObj = NameOfModule(module = module),
      shouldDownload = ModuleDefaults.ShouldDownload,
      versions = Set(module.revision),
      overrideIsEnough = ModuleDefaults.OverrideIsEnough,
      forbidden = None,
      dependenciesToRemove = Seq.empty,
      scalaVersionsFilter = Seq.empty
    )
  }

  def apply(
    org:               String,
    name:              String,
    version:           String,
    needDoublePercent: Boolean
  ): Dependency = {
    Dependency(
      nameObj = NameOfModule(
        org,
        name,
        exactName = ModuleDefaults.ExactName,
        excludeName = Seq.empty,
        needDoublePercent = needDoublePercent
      ),
      shouldDownload = ModuleDefaults.ShouldDownload,
      versions = Set(version),
      overrideIsEnough = ModuleDefaults.OverrideIsEnough,
      dependenciesToRemove = Seq.empty,
      forbidden = None,
      scalaVersionsFilter = Seq.empty
    )
  }

  def apply(input: (String, String)): Dependency = {
    apply(input._1, input._2)
  }

  def apply(
    org:  String,
    name: String
  ): Dependency = {
    Dependency(
      nameObj = NameOfModule(org, name),
      shouldDownload = ModuleDefaults.ShouldDownload,
      versions = Set.empty,
      overrideIsEnough = ModuleDefaults.OverrideIsEnough,
      dependenciesToRemove = Seq.empty,
      forbidden = None,
      scalaVersionsFilter = Seq.empty
    )
  }
}
