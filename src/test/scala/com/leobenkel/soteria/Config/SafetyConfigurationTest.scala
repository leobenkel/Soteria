package com.leobenkel.soteria.Config

import com.leobenkel.soteria.{LogTest, ParentTest}
import com.leobenkel.soteria.Modules.Dependency
import com.leobenkel.soteria.Utils.Json.JsonDecode

class SoteriaConfigurationTest extends ParentTest {
  private val test:       SoteriaConfigurationTest = this
  private val soteriaLog: LogTest                  = new LogTest(test)

  test("test serialize/deserialize") {
    val s =
      SoteriaConfiguration(
        soteriaLog,
        sbtVersion = "sbtV1",
        scalaVersions =
          Set(
            "scala1",
            "scala2",
          ),
        scalaCFlags = Seq("flag1", "flag2"),
        dockerImageOpt = None,
        modules =
          Map(
            "com.org" ->
              Map(
                "name" ->
                  SerializedModule
                    .Empty
                    .copy(
                      version = "1.0",
                      shouldBeProvided = Some(true),
                      excludeName = Some(Seq("a", "b")),
                    )
              )
          ),
      )

    val encodedEi = s.toJsonStructure
    assert(encodedEi.isRight)
    val encoded   = encodedEi.right.get
    log.debug(encoded)
    val sParsedEi = SoteriaConfiguration.parser(soteriaLog)(encoded)
    assert(sParsedEi.isRight)
    val sParsed   = sParsedEi.right.get
    assertEquals(s, sParsed)
  }

  test("test serialize/deserialize json") {
    val s =
      SoteriaConfiguration(
        soteriaLog,
        sbtVersion = "sbtV1",
        scalaVersions =
          Set(
            "scala1",
            "scala2",
          ),
        scalaCFlags = Seq("flag1", "flag2"),
        dockerImageOpt = None,
        modules =
          Map(
            "com.org" ->
              Map(
                "name" ->
                  SerializedModule
                    .Empty
                    .copy(
                      version = "1.0",
                      shouldBeProvided = Some(true),
                      excludeName = Some(Seq("a", "b")),
                    )
              )
          ),
      )

    val encodedEi = JsonDecode.encode(s)
    assert(encodedEi.isRight)
    val encoded   = encodedEi.right.get
    log.debug(encoded)
    val sParsedEi =
      JsonDecode.parse[SoteriaConfiguration](encoded)(
        SoteriaConfiguration.parser(soteriaLog)
      )
    assert(sParsedEi.isRight)
    val sParsed   = sParsedEi.right.get
    assertEquals(s, sParsed)
  }

  test("test replace module - replace") {
    val s1 =
      SoteriaConfiguration(
        soteriaLog,
        sbtVersion = "sbtV1",
        scalaVersions =
          Set(
            "scala1",
            "scala2",
          ),
        scalaCFlags = Seq("flag1", "flag2"),
        dockerImageOpt = None,
        modules =
          Map(
            "com.org" ->
              Map(
                "name" ->
                  SerializedModule
                    .Empty
                    .copy(
                      version = "1.0",
                      shouldBeProvided = Some(true),
                      excludeName = Some(Seq("a", "b")),
                    )
              )
          ),
      )

    val s2 = s1.replaceModule(Dependency("com.org", "name").withVersion("2.0"))

    assert(s2.AllModules.length == 1)
    assert(s2.AllModules.head.version.right.get == "2.0")
  }

  test("test replace module - new artifact") {
    val s1 =
      SoteriaConfiguration(
        soteriaLog,
        sbtVersion = "sbtV1",
        scalaVersions =
          Set(
            "scala1",
            "scala2",
          ),
        scalaCFlags = Seq("flag1", "flag2"),
        dockerImageOpt = None,
        modules =
          Map(
            "com.org" ->
              Map(
                "name" ->
                  SerializedModule
                    .Empty
                    .copy(
                      version = "1.0",
                      shouldBeProvided = Some(true),
                      excludeName = Some(Seq("a", "b")),
                    )
              )
          ),
      )

    val s2 = s1.replaceModule(Dependency("com.org", "name2").withVersion("2.0"))

    assert(s2.AllModules.length == 2)
    assert(s2.AllModules.find(_.name == "name").get.version.right.get == "1.0")
    assert(s2.AllModules.find(_.name == "name2").get.version.right.get == "2.0")
  }

  test("test replace module - new org") {
    val s1 =
      SoteriaConfiguration(
        soteriaLog,
        sbtVersion = "sbtV1",
        scalaVersions =
          Set(
            "scala1",
            "scala2",
          ),
        scalaCFlags = Seq("flag1", "flag2"),
        dockerImageOpt = None,
        modules =
          Map(
            "com.org" ->
              Map(
                "name" ->
                  SerializedModule
                    .Empty
                    .copy(
                      version = "1.0",
                      shouldBeProvided = Some(true),
                      excludeName = Some(Seq("a", "b")),
                    )
              )
          ),
      )

    val s2 = s1.replaceModule(Dependency("com.org2", "name2").withVersion("2.0"))

    assert(s2.AllModules.length == 2)
    assert(s2.AllModules.find(_.name == "name").get.version.right.get == "1.0")
    assert(s2.AllModules.find(_.name == "name2").get.version.right.get == "2.0")
  }
}
