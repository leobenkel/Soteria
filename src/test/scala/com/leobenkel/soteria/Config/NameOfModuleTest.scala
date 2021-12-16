package com.leobenkel.soteria.Config

import com.leobenkel.soteria.Modules.NameOfModule
import org.scalatest.funsuite.AnyFunSuite

class NameOfModuleTest extends AnyFunSuite {
  test("Name match") {
    val filter = NameOfModule(
      organization = "org.orgs",
      name = "foo-",
      exactName = false,
      excludeName = Seq("foo-bar-", "foo-something-hello", "bar"),
      needDoublePercent = false
    )
    Seq(
      (false, "foo"),
      (true, "foo-yoh"),
      (false, "foo-bar-something"),
      (false, "foo-something-hello"),
      (false, "bar"),
      (false, "google"),
      (true, "foo-something-hey-something"),
      (false, "foo-something-hello-hey")
    ).map {
      case (expected, test) =>
        assertResult(
          expected,
          s"Failed for filter: $filter and input $test. Was expected $expected."
        )(filter.nameMatch(test))
    }
  }

  test("Test failed to find module") {
    val searchFor = "This is a broken name"
    val result = NameOfModule.find(
      data = Map(
        "com.org" ->
          Map(
            "artifactName" -> SerializedModule.Empty.copy(version = "1.0")
          )
      )
    )(s = searchFor)

    assert(result.isLeft)
    val error = result.left.get
    assert(error.contains(searchFor))
  }
}
