package com.leobenkel.soteria.Messages

import com.leobenkel.soteria.ParentTest

class ErrorMessageTest extends ParentTest {

  import CommonMessage._

  test("Test errors") {
    "This is an error"
      .asErrors
      .consume(s =>
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
      ).toError("more Errors")).consume(s =>
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
      .consume(s =>
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
      .consume(s =>
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

  test("Test resolve with errors") {
    val input = Errors(
      Seq(
        "error one",
        "error two",
        "error three"
      ).map(_.asError)
    ).resolve(SuccessMessage("This is a success"))

    assert(input.isLeft)
  }

  test("Test resolve without errors") {
    val input = Errors(Seq.empty[WithErrorMessage]).resolve("This is a success")

    assert(input.isRight)
  }

  test("Test add NoError") {
    val error1 = "this is an error".asError
    val noError = NoError

    val allErrors = error1 ++ noError

    allErrors.consume { s =>
      assertEquals(
        """
          |Found blocks of errors (1) :
          |  this is an error
        """.trim.stripMargin,
        s
      )
    }
  }

  test("Test add errors") {
    val errors = Seq(
      "error one",
      "error two",
      "error three"
    ).map(_.asErrors).reduce(_ ++ _)

    errors.consume { s =>
      assertEquals(
        """
          |Found blocks of errors (3) :
          |  error one
          |  error two
          |  error three
        """.trim.stripMargin,
        s
      )
    }
  }

  test("Test Errors with no error") {
    Errors(Seq.empty[WithErrorMessage]).consume(_ => fail("Should not be called"))
    assertEquals("No Error", Errors(Seq.empty[WithErrorMessage]).toString)
  }

  test("Test String to NoError") {
    assertEquals(NoError, Seq.empty[String].toError("Cool title"))
  }

  test("Test combine empty error") {
    val emptyError = NoError
    val error = ErrorMessage("error title", "error Message")
    val allError = (emptyError ++ NoError) ++ (NoError ++ error)

    allError.consume { s =>
      assertEquals(
        """
          |Found blocks of errors (1) :
          |  error title (1) :
          |    error Message
        """.trim.stripMargin,
        s
      )
    }
  }

  test("Test NoError consume") {
    NoError.consume(_ => fail("This should not be called"))
    NoError.toErrors.consume(_ => fail("This should not be called either"))
  }
}
