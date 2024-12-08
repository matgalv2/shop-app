package io.github.g4lowy.order.domain.address

import io.github.g4lowy.order.domain.model.address.Street
import zio.Scope
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

object StreetSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("Street validation should")(
      test("fail when value is empty") {
        val street = Street.Unvalidated("")
        assertTrue(street.validate.isInvalid)
      },
      test("fail when value is blank") {
        val street = Street.Unvalidated("   ")
        assertTrue(street.validate.isInvalid)
      },
      test("fail when value is not capitalized") {
        val street = Street.Unvalidated("ann")
        assertTrue(street.validate.isInvalid)
      },
      test("succeed otherwise ") {
        val street = Street.Unvalidated("Ann")
        assertTrue(street.validate.isValid)
      }
    )
}
