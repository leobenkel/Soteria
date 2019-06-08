package com.leobenkel.safetyplugin.Config

import com.leobenkel.safetyplugin.Modules.NameOfModule
import com.leobenkel.safetyplugin.ParentTest
import com.leobenkel.safetyplugin.Utils.Json.JsonDecode

import scala.io.Source

// scalastyle:off magic.number
class SerializedModuleTest extends ParentTest {
  test("Test parsing to NameOfModule") {
    val pathToFile = "safetyPlugin_succeed_4.json"

    val file = Source.fromResource(pathToFile)
    val content = file.mkString
    file.close()

    println(s"Reading '$pathToFile'")

    val result: Either[String, SafetyConfiguration] =
      JsonDecode.parse[SafetyConfiguration](content)

    assert(result.isRight)

    val serializedModule = result.right.get

    val modules = serializedModule.AllModules.sortBy(s => s.key)

    assertEquals(3, modules.length)

    val m1 = modules.head

    assertEquals("com.orgs", m1.organization)
    assertEquals("name-of-library", m1.name)
    assertEquals(Right("3.0"), m1.version)

    val m2 = modules.apply(2)

    assertEquals("com.other.org", m2.organization)
    assertEquals("artifact-name", m2.name)
    assertEquals(Right("2.1.0"), m2.version)

    val m3 = modules.apply(1)
    val m3Obj = m3.nameObj
    assertEquals("com.other.org", m3.organization)
    assertEquals("artif", m3.name)
    assert(m3.version.isLeft)
    assert(m3.version.left.get.contains("0"))
    assertEquals(false, m3Obj.exactName)
    assertEquals(Seq("artifactory", "artifice"), m3Obj.excludeName.sortBy(identity))
    assertEquals(1, m3.dependenciesToRemove.length)
    assertEquals(Seq(NameOfModule.apply("com.orgs", "name-of-library")), m3.dependenciesToRemove)
  }

  test("test serialize/deserialize") {
    val s = SerializedModule.Empty.copy(
      version = "1.0",
      shouldBeProvided = Some(true),
      excludeName = Some(Seq("a", "b"))
    )
    val encodedEi = s.toJsonStructure
    assert(encodedEi.isRight)
    val encoded = encodedEi.right.get
    println(encoded)
    val sParsedEi = SerializedModule.parser("com.org", "arti")(encoded)
    assert(sParsedEi.isRight)
    val sParsed = sParsedEi.right.get
    assertEquals(s, sParsed)
  }
}
// scalastyle:on magic.number
