package com.leobenkel.soteria.Utils.Json

private[soteria] object JsonParserHelper {
  implicit class GetAs(val m: Map[String, Any]) extends AnyVal {
    def getAs[A](key: String): Either[String, A] = {
      m.get(key).map(_.asInstanceOf[A]) match {
        case Some(v) => Right(v)
        case None    => Left(s"Failed to parse key '$key'")
      }
    }

    def getOption[A](key: String): Either[String, Option[A]] = {
      Right(m.get(key).map(_.asInstanceOf[A]))
    }

    def getAsInt(key: String): Either[String, Int] = {
      m.get(key).map(_.asInstanceOf[Double].toInt) match {
        case Some(v) => Right(v)
        case None    => Left(s"Failed to parse key '$key'")
      }
    }
  }

  implicit class EISequence[A, B](s: Seq[Either[A, B]]) {
    // https://stackoverflow.com/a/7231180/3357831
    @transient lazy val flattenedEiSeq: Either[A, Seq[B]] =
      s.foldRight(Right(Nil): Either[A, List[B]]) { (e, acc) =>
        for {
          xs <- acc.right
          x  <- e.right
        } yield {
          x :: xs
        }
      }
  }

  implicit class EISequenceSoSoDeep[KEY, ERR, VALUE](s: Seq[(KEY, Either[ERR, VALUE])]) {
    // https://stackoverflow.com/a/7231180/3357831
    @transient lazy val flattenedEiSeq: Either[ERR, Seq[(KEY, VALUE)]] =
      s.foldRight(Right(Nil): Either[ERR, Seq[(KEY, VALUE)]]) {
        case ((k, ei), acc) =>
          for {
            ac <- acc
            ae <- ei
          } yield {
            ac :+ (k, ae)
          }
      }
  }

  implicit class EISequenceDeep[KEY, ERR, VALUE](s: Seq[(KEY, Seq[Either[ERR, VALUE]])]) {
    // https://stackoverflow.com/a/7231180/3357831
    @transient lazy val flattenedEiSeq: Either[ERR, Seq[(KEY, Seq[VALUE])]] =
      s.foldRight(Right(Nil): Either[ERR, Seq[(KEY, Seq[VALUE])]]) {
        case ((k, ei), acc) =>
          for {
            ac <- acc
            ae <- ei.flattenedEiSeq
          } yield {
            ac :+ (k, ae)
          }
      }
  }
}
