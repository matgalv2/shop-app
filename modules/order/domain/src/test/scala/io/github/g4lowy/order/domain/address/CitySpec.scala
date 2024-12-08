package io.github.g4lowy.order.domain.address

import io.github.g4lowy.order.domain.model.address.City
import zio.Scope
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

object CitySpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("City validation should")(
      test("fail when value is empty") {
        val city = City.Unvalidated("")
        assertTrue(city.validate.isInvalid)
      },
      test("fail when value is blank") {
        val city = City.Unvalidated("   ")
        assertTrue(city.validate.isInvalid)
      },
      test("fail when value is not capitalized") {
        val city = City.Unvalidated("ann")
        assertTrue(city.validate.isInvalid)
      },
      test("succeed otherwise ") {
        val city = City.Unvalidated("Ann")
        assertTrue(city.validate.isValid)
      }
    )
}
