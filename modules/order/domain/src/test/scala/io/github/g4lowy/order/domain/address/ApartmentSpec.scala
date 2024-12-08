package io.github.g4lowy.order.domain.address

import io.github.g4lowy.order.domain.model.address.Apartment
import zio.Scope
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

object ApartmentSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("Apartment validation should")(
      test("fail when value is empty") {
        val apartment = Apartment.Unvalidated("")
        assertTrue(apartment.validate.isInvalid)
      },
      test("fail when value is blank") {
        val apartment = Apartment.Unvalidated("   ")
        assertTrue(apartment.validate.isInvalid)
      },
      test("fail when value does not match pattern") {
        val apartment = Apartment.Unvalidated("12 e")
        assertTrue(apartment.validate.isInvalid)
      },
      test("succeed otherwise ") {
        val apartment = Apartment.Unvalidated("12")
        assertTrue(apartment.validate.isValid)
      }
    )
}
