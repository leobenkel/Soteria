package com.leobenkel.soteria.Modules

import scala.reflect.ClassTag

object ModuleDefaults {
  val ShouldDownload:    Boolean = true
  val OverrideIsEnough:  Boolean = true
  val ShouldBeProvided:  Boolean = false
  val ExactName:         Boolean = true
  val NeedDoublePercent: Boolean = false

  def toOptionWithDefault[A](
    defaultValue: A,
    currentValue: A
  ): Option[A] = if (defaultValue == currentValue) None else Some(currentValue)

  def toOption[A: ClassTag](currentArray: Seq[A]): Option[Seq[A]] =
    if (currentArray.isEmpty) None else Some(currentArray)
}
