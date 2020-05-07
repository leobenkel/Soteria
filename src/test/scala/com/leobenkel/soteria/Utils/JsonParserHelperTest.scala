package com.leobenkel.soteria.Utils

import com.leobenkel.soteria.ParentTest

class JsonParserHelperTest extends ParentTest {
  import com.leobenkel.soteria.Utils.Json.JsonParserHelper._

  test("Test fail to find key for Int") {
    val input: Map[String, Any] = Map.empty
    val key = "unknown key"
    val output = input.getAsInt(key)
    assert(output.isLeft)
    assert(output.left.get.contains(key))
  }
}
