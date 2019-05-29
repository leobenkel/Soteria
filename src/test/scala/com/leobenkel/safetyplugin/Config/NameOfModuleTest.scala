package com.leobenkel.safetyplugin.Config

import com.leobenkel.safetyplugin.Modules.NameOfModule
import org.scalatest.FunSuite

class NameOfModuleTest extends FunSuite {
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
}
