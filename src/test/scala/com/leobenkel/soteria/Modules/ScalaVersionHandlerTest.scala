package com.leobenkel.soteria.Modules

import com.leobenkel.soteria.Modules.ScalaVersionHandler._
import com.leobenkel.soteria.ParentTest
import com.leobenkel.soteria.Utils.EitherUtils._

class ScalaVersionHandlerTest extends ParentTest {

  test("Test parse ScalaVersionHandler - simple") {
    val input = "+2.12"
    val sv    = ScalaVersionHandler(input)
    assert(sv.isRight)
    val svR   = sv.right.get
    assertResult(input)(svR.serialized)
    assert(svR.filterPositive)
    assertResult("2")(svR.scalaVersion.scalaVersionMajor)
    assertResult("12")(svR.scalaVersion.scalaVersionMinor)
    assertResult(None)(svR.scalaVersion.scalaVersionMin)
  }

  test("Test parse ScalaVersionHandler - with min version") {
    val input = "+2.12.11"
    val sv    = ScalaVersionHandler(input)
    assert(sv.isRight)
    val svR   = sv.right.get
    assertResult(input)(svR.serialized)
    assert(svR.filterPositive)
    assertResult("2")(svR.scalaVersion.scalaVersionMajor)
    assertResult("12")(svR.scalaVersion.scalaVersionMinor)
    assertResult(Some("11"))(svR.scalaVersion.scalaVersionMin)
  }

  test("Test parse ScalaVersionHandler - with negative") {
    val input = "-2.12.11"
    val sv    = ScalaVersionHandler(input)
    assert(sv.isRight)
    val svR   = sv.right.get
    assertResult(input)(svR.serialized)
    assert(!svR.filterPositive)
    assertResult("2")(svR.scalaVersion.scalaVersionMajor)
    assertResult("12")(svR.scalaVersion.scalaVersionMinor)
    assertResult(Some("11"))(svR.scalaVersion.scalaVersionMin)
  }

  test("Test parse ScalaVersionHandler - failed parsing - bad filter") {
    val input = "2.12.11"
    val sv    = ScalaVersionHandler(input)
    assert(sv.isLeft)
    println(sv)
  }

  test("Test parse ScalaVersionHandler - failed parsing - bad version 1") {
    val input = "+2"
    val sv    = ScalaVersionHandler(input)
    assert(sv.isLeft)
    println(sv)
  }

  test("Test parse ScalaVersionHandler - failed parsing - bad version 2") {
    val input = "+2.1231+123"
    val sv    = ScalaVersionHandler(input)
    assert(sv.isLeft)
    println(sv)
  }

  test("Test parse ScalaVersionHandler - failed parsing - bad version 3") {
    val input = "+foo"
    val sv    = ScalaVersionHandler(input)
    assert(sv.isLeft)
    println(sv)
  }

  test("Test right scala version - positive") {
    val filters =
      Seq(
        ScalaVersionHandler("+2.12"),
        ScalaVersionHandler("+2.12.11"),
        ScalaVersionHandler("+2.11.7"),
        ScalaVersionHandler("+2.10"),
      ).flattenEI.applyTo(_)

    assert(ScalaV("2.12").right.map(filters(_)).right.get)
    assert(ScalaV("2.12.11").right.map(filters(_)).right.get)
    assert(ScalaV("2.13.8").right.map(filters(_)).right.get)
    assert(!ScalaV("2.7.15").right.map(filters(_)).right.get)
  }

  test("Test right scala version - positive 2") {
    val filters =
      Seq(
        ScalaVersionHandler("+2.12")
      ).flattenEI.applyTo(_)

    assert(!ScalaV("2.11").right.map(filters(_)).right.get)
    assert(ScalaV("2.12.11").right.map(filters(_)).right.get)
    assert(ScalaV("2.13.8").right.map(filters(_)).right.get)
    assert(!ScalaV("2.7.15").right.map(filters(_)).right.get)
    assert(!ScalaV("2.11.15").right.map(filters(_)).right.get)
  }

  test("Test right scala version - negative") {
    val filters =
      Seq(
        ScalaVersionHandler("-2.12"),
        ScalaVersionHandler("-2.12.11"),
        ScalaVersionHandler("-2.11.7"),
        ScalaVersionHandler("-2.10"),
      ).flattenEI.applyTo(_)

    assert(!ScalaV("2.12").right.map(filters(_)).right.get)
    assert(!ScalaV("2.12.11").right.map(filters(_)).right.get)
    assert(!ScalaV("2.13.8").right.map(filters(_)).right.get)
    assert(ScalaV("2.7.15").right.map(filters(_)).right.get)
  }

  test("Test right scala version - mix") {
    val f =
      Seq(
        ScalaVersionHandler("+2.12"),
        ScalaVersionHandler("-2.12.11"),
        ScalaVersionHandler("-2.11.7"),
        ScalaVersionHandler("-2.10"),
      ).flattenEI

    val filters = f.applyTo(_)

    assert(!ScalaV("2.12").right.map(filters(_)).right.get)
    assert(!ScalaV("2.12.11").right.map(filters(_)).right.get)
    assert(ScalaV("2.13.8").right.map(filters(_)).right.get)
    assert(!ScalaV("2.7.15").right.map(filters(_)).right.get)
  }
}
