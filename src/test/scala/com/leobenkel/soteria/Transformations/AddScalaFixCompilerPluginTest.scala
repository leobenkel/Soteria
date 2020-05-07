package com.leobenkel.soteria.Transformations

import com.leobenkel.soteria.ParentTest
import com.leobenkel.soteria.ParentTest

class AddScalaFixCompilerPluginTest extends ParentTest with AddScalaFixCompilerPlugin {
  test("Test correct version") {
    assert(shouldAddCompilerPlugin("2.12.11"))
    assert(shouldAddCompilerPlugin("2.11.7"))
    assert(shouldAddCompilerPlugin("2.10.5"))
    assert(!shouldAddCompilerPlugin("2.13.11"))
    assert(!shouldAddCompilerPlugin("2.13.1"))
    assert(!shouldAddCompilerPlugin("foo"))
    assert(!shouldAddCompilerPlugin(""))
  }
}
