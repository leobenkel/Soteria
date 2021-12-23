package com.leobenkel.soteria.Utils.Json

object FilterNulls {
  implicit class FilterMap(val m: Map[_, _]) extends AnyVal {
    def filterNullsOut: Map[String, Any] =
      m.map { case (k, v) => (k.toString, v) }
        .filter {
          case (_, v: Option[_]) => v.isDefined
          case (_, v: List[_])   => v.nonEmpty
          case (_, v: Map[_, _]) => v.nonEmpty
          case (_, _)            => true
        }
        .mapValues {
          case v: Some[_] => v.get
          case v => v
        }
  }
}
