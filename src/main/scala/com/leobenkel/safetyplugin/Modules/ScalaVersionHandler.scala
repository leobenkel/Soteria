package com.leobenkel.safetyplugin.Modules

import com.leobenkel.safetyplugin.Modules.ScalaVSimilarity._
import com.leobenkel.safetyplugin.Utils.ListUtils._
import sbt.CrossVersion

import scala.util.matching.Regex

case class ScalaVersionHandler(
  filterPositive: Boolean,
  scalaVersion:   ScalaV
) {
  @transient lazy final val serialized: String =
    (if (filterPositive) '+' else '-') +: scalaVersion.serialized
}

object ScalaVersionHandler {
  implicit class ScalaFilters(input: Seq[ScalaVersionHandler]) {
    def applyTo(scalaV: ScalaV): Boolean = {
      if (input.isEmpty) {
        true
      } else {
        input
          .groupBy(_.filterPositive)
          .map {
            case (true, filters) =>
              filters
                .flatMap(_.scalaVersion is scalaV)
                .toOption
                .flatMap(_.minOption)
                .map(sim => (Some(sim), true))
                .getOrElse((Some(Wildcard), false))
            case (false, filters) =>
              (filters.flatMap(_.scalaVersion is scalaV).minOption, false)
          }
          .filter(_._1.isDefined)
          .toOption
          .forall(_.minBy(_._1.get)._2)
      }
    }
  }

  private def getFilter(input: String): Either[String, (String, Boolean)] = {
    input.toCharArray.toList match {
      case '+' :: sVersion => Right((sVersion.mkString(""), true))
      case '-' :: sVersion => Right((sVersion.mkString(""), false))
      case _ =>
        Left(
          s"Unable to parse '$input', must start by '+' or '-'," +
            s" expected structure: '[+-][major].[minor].<min>'"
        )
    }
  }

  def apply(input: String): Either[String, ScalaVersionHandler] = {
    for {
      filterOutput <- getFilter(input)
      rest = filterOutput._1
      filter = filterOutput._2
      scalaVersion <- ScalaV(rest)
    } yield {
      ScalaVersionHandler(filter, scalaVersion)
    }
  }
}

object ScalaVSimilarity {
  sealed abstract class Sim(protected val value: Int) extends Ordered[Sim] {
    override def compare(that: Sim): Int = {
      this.value.compare(that.value)
    }
  }

  case object ExactSame extends Sim(0)
  case object MightBeExact extends Sim(1)
  case object MajorSame extends Sim(2)
  case object Wildcard extends Sim(999)
}

case class ScalaV(
  scalaVersionMajor: String,
  scalaVersionMinor: String,
  scalaVersionMin:   Option[String]
) {
  @transient lazy final val scalaBinaryVersion: String =
    (scalaVersionMajor :: scalaVersionMinor :: Nil).mkString(".")
  @transient lazy final val serialized: String =
    ((scalaVersionMajor :: scalaVersionMinor :: Nil) ++ scalaVersionMin).mkString(".")
  @transient lazy final val crossVersion =
    CrossVersion.apply(scalaFullVersion = serialized, scalaBinaryVersion = scalaBinaryVersion)

  def is(input: ScalaV): Option[Sim] = {
    input match {
      case StrongEqual(true)  => Some(ExactSame)
      case CouldBeEqual(true) => Some(MightBeExact)
      case MajorEqual(true)   => Some(MajorSame)
      case _                  => None
    }
  }

  object MajorEqual {
    def unapply(input: ScalaV): Option[Boolean] = {
      input match {
        case ScalaV(ma, mi, None) => Some(scalaVersionMajor == ma && scalaVersionMinor == mi)
        case _                    => None
      }
    }
  }

  object CouldBeEqual {
    def unapply(input: ScalaV): Option[Boolean] = {
      input match {
        case ScalaV(ma, mi, Some(mm)) =>
          Some(
            scalaVersionMajor == ma && scalaVersionMinor == mi && scalaVersionMin.forall(_ == mm)
          )
        case _ => None
      }
    }
  }

  object StrongEqual {
    def unapply(input: ScalaV): Option[Boolean] = {
      input match {
        case ScalaV(ma, mi, Some(mm)) =>
          Some(
            scalaVersionMajor == ma && scalaVersionMinor == mi && scalaVersionMin.contains(mm)
          )
        case _ => None
      }
    }
  }

  def ===(moduleScalaVersion: String): Boolean = this === ScalaV(moduleScalaVersion)

  def ===(input: ScalaV): Boolean = {
    input match {
      case CouldBeEqual(t) => t
      case MajorEqual(t)   => t
      case _               => false
    }
  }

  def ===(input: Either[String, ScalaV]): Boolean = {
    input match {
      case Right(sv) => this === sv
      case _         => false
    }
  }
}

object ScalaV {
  private val NR: Regex = "([0-9]+)".r
  def apply(input: String): Either[String, ScalaV] = {
    input.split('.').toList match {
      case NR(major) :: NR(minor) :: Nil            => Right(ScalaV(major, minor, None))
      case NR(major) :: NR(minor) :: NR(min) :: Nil => Right(ScalaV(major, minor, Some(min)))
      case _ =>
        Left(s"Unable to parse '$input', expected structure: '[major].[minor].<min>'")

    }
  }
}
