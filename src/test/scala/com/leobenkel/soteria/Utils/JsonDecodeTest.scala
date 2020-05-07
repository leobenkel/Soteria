package com.leobenkel.soteria.Utils

import com.leobenkel.soteria.Config.SoteriaConfiguration
import com.leobenkel.soteria.Utils.Json.JsonDecode
import com.leobenkel.soteria.Utils.Json.JsonParserHelper._
import com.leobenkel.soteria.{LogTest, ParentTest}
import org.scalatest.Assertion

import scala.io.Source

class JsonDecodeTest extends ParentTest {
  private val soteriaLog: LogTest = new LogTest(this)

  test("Test decode json") {
    val value: Int = 12
    case class MyJson(key: Int)

    implicit val parser: JsonDecode.Parser[MyJson] = (input: Map[String, Any]) => {
      for {
        key <- input.getAsInt("key")
      } yield {
        MyJson(key)
      }
    }

    val ei = JsonDecode.parse[MyJson](s"""
        |{
        |"key": $value
        |}
      """.stripMargin)

    assert(ei.isRight)
    assertEquals(value, ei.right.get.key)
  }

  test("Test decode soteria.json") {
    Map[String, Either[String, SoteriaConfiguration] => Assertion](
      "soteria_succeed_1.json" -> { result =>
        assert(result.isRight)
        val parsed = result.right.get
        assert(parsed.modules.size == 1)
        assert(parsed.modules.head._2.size == 1)
        assert(parsed.modules.head._2.head._2.version == "3.0")
        assert(parsed.scalaCFlags.length == 10)
        assert(parsed.scalaVersions.size == 2)
      },
      "soteria_succeed_2.json" -> { result =>
        assert(result.isRight)
        val parsed = result.right.get
        assert(parsed.modules.size == 1)
        assert(parsed.modules.head._2.size == 1)
        assert(parsed.modules.head._2.head._2.version == "3.0")
        assert(parsed.scalaCFlags.isEmpty)
        assert(parsed.scalaVersions.size == 2)
      },
      "soteria_succeed_3.json" -> { result =>
        assert(result.isRight)
        val parsed = result.right.get
        assert(parsed.modules.isEmpty)
        assert(parsed.scalaCFlags.isEmpty)
        assert(parsed.scalaVersions.size == 2)
      },
      "soteria_fail_no_scalaVersions.json" -> { result =>
        assert(result.isLeft)
        val error = result.left.get
        assert(error.contains("scalaVersions"))
      },
      "soteria_fail_no_version.json" -> { result =>
        assert(result.isLeft)
        val error = result.left.get
        assert(error.contains("version"))
        assert(error.contains("com.orgs"))
        assert(error.contains("name-of-library"))
      },
      "soteria_fail_bad_json.json" -> { result =>
        assert(result.isLeft)
        val error = result.left.get
        assert("Did not parse" == error)
      }
    ).map {
      case (filePath, test) =>
        val file = Source.fromResource(filePath)
        val content = file.mkString
        file.close()

        log.debug(s"Reading '$filePath'")

        val result: Either[String, SoteriaConfiguration] =
          JsonDecode.parse[SoteriaConfiguration](content)(SoteriaConfiguration.parser(soteriaLog))

        test(result)
    }
  }
}
