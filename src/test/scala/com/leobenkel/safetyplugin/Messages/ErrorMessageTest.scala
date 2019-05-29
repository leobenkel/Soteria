package com.leobenkel.safetyplugin.Messages

import com.leobenkel.safetyplugin.ParentTest

class ErrorMessageTest extends ParentTest {

  import CommonMessage._

  test("Test errors") {

    "This is an error".asErrors
      .consume(
        s =>
          assertEquals(
            """
            |Found blocks of errors (1) :
            |  This is an error
          """.trim.stripMargin,
            s
          )
      )
  }

  test("Test more errors") {
    (Seq(
      "error one",
      "error two",
      "error three"
    ).toError("We have a log of errors") ++
      Seq(
        "more 1",
        "more 2",
        "more 3"
      ).toError("more Errors"))
      .consume(
        s =>
          assertEquals(
            """
            |Found blocks of errors (2) :
            |  We have a log of errors (3) :
            |    error one
            |    error two
            |    error three
            |  more Errors (3) :
            |    more 1
            |    more 2
            |    more 3
          """.trim.stripMargin,
            s
          )
      )
  }

  test("Test one nested error") {
    Seq(
      "error one",
      "error two",
      "error three"
    ).toError("We have a log of errors")
      .toErrors
      .consume(
        s =>
          assertEquals(
            """
            |Found blocks of errors (1) :
            |  We have a log of errors (3) :
            |    error one
            |    error two
            |    error three
          """.trim.stripMargin,
            s
          )
      )
  }

  test("Test one error") {
    Seq(
      "error one",
      "error two",
      "error three"
    ).toError("We have a log of errors")
      .consume(
        s =>
          assertEquals(
            """
          |We have a log of errors (3) :
          |  error one
          |  error two
          |  error three
        """.trim.stripMargin,
            s
          )
      )
  }
}
