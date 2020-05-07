package com.leobenkel.soteria.Utils

object EitherUtils {
  implicit class FlattenEither[ERR, VALUE](ei: Seq[Either[ERR, VALUE]]) {
    @transient lazy val flattenEI:   Seq[VALUE] = ei.filter(_.isRight).map(_.right.get)
    @transient lazy val flattenLeft: Seq[ERR] = ei.filter(_.isLeft).map(_.left.get)
  }
}
