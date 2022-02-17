package com.leobenkel.soteria.Config

import com.leobenkel.soteria.{LogTest, ParentTest}
import com.leobenkel.soteria.Modules.NameOfModule
import com.leobenkel.soteria.Utils.Json.JsonDecode
import sbt.util.Level
import scala.io.Source

// scalastyle:off magic.number
class SerializedModuleTest extends ParentTest {
  private val test: SerializedModuleTest = this

  private class LogTestWithBuffer extends LogTest(test) {
    private var allMessages: String = ""

    override def log(
        level:   Level.Value,
        message: => String,
    ): Unit = {
      test.log.debug(message)
      assertEquals(Level.Error, level)
      allMessages += message + "\n"
    }

    def getMessages: String = allMessages
  }

  test("Test parsing to NameOfModule") {
    val pathToFile = "soteria_succeed_4.json"
    val soteriaLog = new LogTestWithBuffer

    val file    = Source.fromResource(pathToFile)
    val content = file.mkString
    file.close()

    log.debug(s"Reading '$pathToFile'")

    val result: Either[String, SoteriaConfiguration] =
      JsonDecode.parse[SoteriaConfiguration](content)(
        SoteriaConfiguration.parser(soteriaLog)
      )

    assert(result.isRight)

    val serializedModule = result.right.get

    assert(soteriaLog.getMessages.isEmpty)

    val modules = serializedModule.AllModules.sortBy(s => s.key)

    val modulesWithDependanceErrors =
      serializedModule.ZTestOnly.RawModulesTest.filter(_._2.nonEmpty)
    modulesWithDependanceErrors.foreach {
      case (module, errors) =>
        assert(soteriaLog.getMessages.contains(module.toString))
        errors.foreach(e => assert(soteriaLog.getMessages.contains(e)))
    }

    assertEquals(3, modules.length)

    val m1 = modules.head

    assertEquals("com.orgs", m1.organization)
    assertEquals("name-of-library", m1.name)
    assertEquals(Right("3.0"), m1.version)

    val m2 = modules.apply(2)

    assertEquals("com.other.org", m2.organization)
    assertEquals("artifact-name", m2.name)
    assertEquals(Right("2.1.0"), m2.version)

    val m3    = modules.apply(1)
    val m3Obj = m3.nameObj
    assertEquals("com.other.org", m3.organization)
    assertEquals("artif", m3.name)
    assert(m3.version.isLeft)
    assert(m3.version.left.get.contains("0"))
    assertEquals(false, m3Obj.exactName)
    assertEquals(
      Seq("artifactory", "artifice"),
      m3Obj.excludeName.sortBy(identity),
    )
    assertEquals(1, m3.dependenciesToRemove.length)
    assertEquals(
      Seq(NameOfModule.apply("com.orgs", "name-of-library")),
      m3.dependenciesToRemove,
    )
  }

  test("test serialize/deserialize") {
    val s         =
      SerializedModule
        .Empty
        .copy(
          version = "1.0",
          shouldBeProvided = Some(true),
          excludeName = Some(Seq("a", "b")),
        )
    val encodedEi = s.toJsonStructure
    assert(encodedEi.isRight)
    val encoded   = encodedEi.right.get
    log.debug(encoded)
    val sParsedEi = SerializedModule.parser("com.org", "arti")(encoded)
    assert(sParsedEi.isRight)
    val sParsed   = sParsedEi.right.get
    assertEquals(s, sParsed)
  }
}
// scalastyle:on magic.number
