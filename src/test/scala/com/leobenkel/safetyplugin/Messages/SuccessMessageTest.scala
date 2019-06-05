package com.leobenkel.safetyplugin.Messages

import com.leobenkel.safetyplugin.ParentTest

class SuccessMessageTest extends ParentTest {
  test("Test SuccessMessage") {
    val content = "content of the message"
    val message = SuccessMessage(content)

    message.consume { s =>
      assertEquals(content, s)
    }
  }
}
