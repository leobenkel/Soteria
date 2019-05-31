package com.leobenkel.safetyplugin.Modules

import com.leobenkel.safetyplugin.ParentTest

class ModuleDefaultsTest extends ParentTest {
  test("Test ToOption with Default") {
    assertEquals(None, ModuleDefaults.toOptionWithDefault("a", "a"))
    assertEquals(Some("b"), ModuleDefaults.toOptionWithDefault("a", "b"))
  }

  test("Test ToOption For Arrays") {
    assertEquals(None, ModuleDefaults.toOption(Seq.empty[String]))
    assertEquals(Some(Seq("a", "b")), ModuleDefaults.toOption(Seq("a", "b")).map(_.toSeq))
  }
}
