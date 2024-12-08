package io.github.g4lowy.order.domain.address

import io.github.g4lowy.order.domain.model.address.Building
import zio.Scope
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

object BuildingSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("Building validation should")(
      test("fail when value is empty") {
        val building = Building.Unvalidated("")
        assertTrue(building.validate.isInvalid)
      },
      test("fail when value is blank") {
        val building = Building.Unvalidated("   ")
        assertTrue(building.validate.isInvalid)
      },
      test("fail when value does not match pattern") {
        val building = Building.Unvalidated("12 e")
        assertTrue(building.validate.isInvalid)
      },
      test("succeed otherwise ") {
        val building = Building.Unvalidated("12")
        assertTrue(building.validate.isValid)
      },
      test("succeed otherwise ") {
        val building = Building.Unvalidated("12345ewq")
        assertTrue(building.validate.isValid)
      }
    )
}
