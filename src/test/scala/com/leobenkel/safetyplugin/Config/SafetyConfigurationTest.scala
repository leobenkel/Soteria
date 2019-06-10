package com.leobenkel.safetyplugin.Config

import com.leobenkel.safetyplugin.Modules.Dependency
import com.leobenkel.safetyplugin.ParentTest
import com.leobenkel.safetyplugin.Utils.Json.JsonDecode

class SafetyConfigurationTest extends ParentTest {

  test("test serialize/deserialize") {
    val s = SafetyConfiguration(
      sbtVersion = "sbtV1",
      scalaVersions = Set(
        "scala1",
        "scala2"
      ),
      scalaCFlags = Seq("flag1", "flag2"),
      dockerImageOpt = None,
      modules = Map(
        "com.org" -> Map(
          "name" -> SerializedModule.Empty.copy(
            version = "1.0",
            shouldBeProvided = Some(true),
            excludeName = Some(Seq("a", "b"))
          )
        )
      )
    )

    val encodedEi = s.toJsonStructure
    assert(encodedEi.isRight)
    val encoded = encodedEi.right.get
    println(encoded)
    val sParsedEi = SafetyConfiguration.parser(encoded)
    assert(sParsedEi.isRight)
    val sParsed = sParsedEi.right.get
    assertEquals(s, sParsed)
  }

  test("test serialize/deserialize json") {
    val s = SafetyConfiguration(
      sbtVersion = "sbtV1",
      scalaVersions = Set(
        "scala1",
        "scala2"
      ),
      scalaCFlags = Seq("flag1", "flag2"),
      dockerImageOpt = None,
      modules = Map(
        "com.org" -> Map(
          "name" -> SerializedModule.Empty.copy(
            version = "1.0",
            shouldBeProvided = Some(true),
            excludeName = Some(Seq("a", "b"))
          )
        )
      )
    )

    val encodedEi = JsonDecode.encode(s)
    assert(encodedEi.isRight)
    val encoded = encodedEi.right.get
    println(encoded)
    val sParsedEi = JsonDecode.parse[SafetyConfiguration](encoded)
    assert(sParsedEi.isRight)
    val sParsed = sParsedEi.right.get
    assertEquals(s, sParsed)
  }

  test("test replace module - replace") {
    val s1 = SafetyConfiguration(
      sbtVersion = "sbtV1",
      scalaVersions = Set(
        "scala1",
        "scala2"
      ),
      scalaCFlags = Seq("flag1", "flag2"),
      dockerImageOpt = None,
      modules = Map(
        "com.org" -> Map(
          "name" -> SerializedModule.Empty.copy(
            version = "1.0",
            shouldBeProvided = Some(true),
            excludeName = Some(Seq("a", "b"))
          )
        )
      )
    )

    val s2 = s1.replaceModule(Dependency("com.org", "name").withVersion("2.0"))

    assert(s2.AllModules.length == 1)
    assert(s2.AllModules.head.version.right.get == "2.0")
  }

  test("test replace module - new artifact") {
    val s1 = SafetyConfiguration(
      sbtVersion = "sbtV1",
      scalaVersions = Set(
        "scala1",
        "scala2"
      ),
      scalaCFlags = Seq("flag1", "flag2"),
      dockerImageOpt = None,
      modules = Map(
        "com.org" -> Map(
          "name" -> SerializedModule.Empty.copy(
            version = "1.0",
            shouldBeProvided = Some(true),
            excludeName = Some(Seq("a", "b"))
          )
        )
      )
    )

    val s2 = s1.replaceModule(Dependency("com.org", "name2").withVersion("2.0"))

    assert(s2.AllModules.length == 2)
    assert(s2.AllModules.find(_.name == "name").get.version.right.get == "1.0")
    assert(s2.AllModules.find(_.name == "name2").get.version.right.get == "2.0")
  }

  test("test replace module - new org") {
    val s1 = SafetyConfiguration(
      sbtVersion = "sbtV1",
      scalaVersions = Set(
        "scala1",
        "scala2"
      ),
      scalaCFlags = Seq("flag1", "flag2"),
      dockerImageOpt = None,
      modules = Map(
        "com.org" -> Map(
          "name" -> SerializedModule.Empty.copy(
            version = "1.0",
            shouldBeProvided = Some(true),
            excludeName = Some(Seq("a", "b"))
          )
        )
      )
    )

    val s2 = s1.replaceModule(Dependency("com.org2", "name2").withVersion("2.0"))

    assert(s2.AllModules.length == 2)
    assert(s2.AllModules.find(_.name == "name").get.version.right.get == "1.0")
    assert(s2.AllModules.find(_.name == "name2").get.version.right.get == "2.0")
  }
}
