package com.leobenkel.soteria.Utils

object ListUtils {
  implicit class ListMinMaxSafe[A: Ordering](input: Seq[A]) {
    def minOption: Option[A] = if (input.isEmpty) None else Some(input.min)
    def maxOption: Option[A] = if (input.isEmpty) None else Some(input.max)
  }

  implicit class ListToOption[A](input: Seq[A]) {
    def toOption: Option[Seq[A]] = if (input.isEmpty) None else Some(input)
  }

  implicit class MapToOption[A, B](input: Map[A, B]) {
    def toOption: Option[Map[A, B]] = if (input.isEmpty) None else Some(input)
  }
}
