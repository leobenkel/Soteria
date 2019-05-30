package com.leobenkel.safetyplugin.Utils

object EitherUtils {
  implicit class FlattenEither[ERR, VALUE](ei: Seq[Either[ERR, VALUE]]) {
    @transient lazy val flattenEI: Seq[VALUE] = ei.filter(_.isRight).map(_.right.get)
  }
}
