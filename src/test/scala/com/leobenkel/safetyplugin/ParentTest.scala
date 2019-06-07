package com.leobenkel.safetyplugin

import org.scalactic.source.Position
import org.scalatest.{FunSuite, Tag}

/**
  * Common methods for all our tests.
  */
trait ParentTest extends FunSuite {
  protected def assertEquals[T](
    expected: T,
    result:   T
  )(
    implicit pos: Position
  ): Unit = {
    assertResult(expected)(result)
    ()
  }

  override protected def test(
    testName: String,
    testTags: Tag*
  )(
    testFun: => Any
  )(
    implicit pos: Position
  ): Unit = {
    super.test(testName, testTags: _*) {
      println(s">>> Starting - $testName")
      testFun
    }
  }

  def time[R](block: => R): (R, Long) = {
    val t0 = System.nanoTime()
    val result = block
    val t1 = System.nanoTime()
    val time_ns: Long = t1 - t0
    (result, time_ns)
  }
}
