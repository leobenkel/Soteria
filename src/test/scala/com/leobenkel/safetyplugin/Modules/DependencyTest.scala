package com.leobenkel.safetyplugin.Modules

import com.leobenkel.safetyplugin.ParentTest

class DependencyTest extends ParentTest {
  test("Test to ModuleID") {
    val org = "com.org"
    val name = "artifactName"
    val version = "1.0"
    val d = Dependency.apply(org, name)
    val moduleEi = d.toModuleID(version)

    assert(moduleEi.isRight)

    val module = moduleEi.right.get
    assertEquals(org, module.organization)
    assertEquals(name, module.name)
    assertEquals(version, module.revision)
  }

  test("Test to ModuleID - when impossible") {
    val org = "com.org"
    val name = "artifactName"
    val version = "1.0"
    val d = Dependency.apply(org, name).withName(_.copy(exactName = false))
    val moduleEi = d.toModuleID(version)

    assert(moduleEi.isLeft)
    val error = moduleEi.left.get
    assert(error.contains(org))
    assert(error.contains(name))
  }

  test("Test comparator with ModuleID") {
    val org = "com.org"
    val name = "artifactName"
    val version = "1.0"
    val d = Dependency.apply(org, name)
    val moduleId = d.toModuleID(version).right.get

    assert(d === moduleId)
    assert(d.withName(_.copy(name = "a different name")) =!= moduleId)
  }

  test("Test combine") {
    val org = "com.org"
    val name = "artifactName"
    val version1 = "1.0"
    val version2 = "2.0"

    val d = Dependency.apply(org, name)
    val d1 = d.withVersion(version = version1)
    val d2 = d.withVersion(version = version2)

    val dAllEi = d1 |+| d2

    assert(dAllEi.isRight)

    val dAll = dAllEi.right.get
    assert(d === dAll)
    assert(dAll.versions.size == 2)
    assert(dAll.versions.contains(version1))
    assert(dAll.versions.contains(version2))
  }

  test("Test combine - same version") {
    val org = "com.org"
    val name = "artifactName"
    val version = "1.0"

    val d = Dependency.apply(org, name)
    val d1 = d.withVersion(version = version)
    val d2 = d.withVersion(version = version)

    val dAllEi = d1 |+| d2

    assert(dAllEi.isRight)

    val dAll = dAllEi.right.get
    assert(d === dAll)
    assert(dAll.versions.size == 1)
    assert(dAll.versions.contains(version))
  }

  test("Test combine fail") {
    val org = "com.org"
    val name1 = "artifactName"
    val name2 = "different-name"
    val version1 = "1.0"
    val version2 = "1.0"

    val d1 = Dependency.apply(org, name1).withVersion(version = version1)
    val d2 = Dependency.apply(org, name2).withVersion(version = version2)

    val dAllEi = d1 |+| d2

    assert(dAllEi.isLeft)

    val error = dAllEi.left.get
    assert(error.contains(org))
    assert(error.contains(name1))
    assert(error.contains(name2))
    assert(error.contains(version1))
    assert(error.contains(version2))
  }

  test("Test constructor with needDoublePercent") {
    val org = "com.org"
    val name = "artifactName"
    val version = "1.0"

    val m = Dependency.apply(org, name, version, needDoublePercent = true)

    assertEquals(org, m.organization)
    assertEquals(name, m.name)
    assertEquals(Right(version), m.version)
    assertEquals(true, m.nameObj.needDoublePercent)
  }
}
