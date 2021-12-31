package com.leobenkel.soteria.Messages

import com.leobenkel.soteria.ParentTest

class SuccessMessageTest extends ParentTest {
  test("Test SuccessMessage") {
    val content = "content of the message"
    val message = SuccessMessage(content)

    message.consume(s => assertEquals(content, s))
  }
}
