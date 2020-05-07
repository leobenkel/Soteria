package com.leobenkel.soteria.Config

import com.leobenkel.soteria.Modules._
import com.leobenkel.soteria.ParentTest
import sbt._

class ModuleNoVersionTest extends ParentTest {
  test("Test ModuleNoVersionEquals") {
    val module1 = Dependency(
      "org",
      "name"
    )

    assertEquals(true, module1 === "org"  % "name"      % "version")
    assertEquals(false, module1 === "org" % "name-core" % "version")
    assertEquals(false, module1 === "org" % "different" % "version")

    val module2 = Dependency(
      "org",
      "name-"
    ).withName(_.copy(exactName = false))

    assertEquals(true, module2 === "org"  % "name-core" % "version")
    assertEquals(true, module2 === "org"  % "name-all"  % "version")
    assertEquals(false, module2 === "org" % "name"      % "version")
    assertEquals(false, module2 === "org" % "different" % "version")

    val module3 = Dependency(
      "org",
      "name-"
    ).withName(
      _.copy(
        excludeName = Seq("name-all"),
        exactName = false
      )
    )

    assertEquals(true, module3 === "org"  % "name-core"  % "version")
    assertEquals(true, module3 === "org"  % "name-alok"  % "version")
    assertEquals(false, module3 === "org" % "name-all"   % "version")
    assertEquals(false, module3 === "org" % "name-all-2" % "version")
    assertEquals(false, module3 === "org" % "name"       % "version")
    assertEquals(false, module3 === "org" % "different"  % "version")
  }
}
