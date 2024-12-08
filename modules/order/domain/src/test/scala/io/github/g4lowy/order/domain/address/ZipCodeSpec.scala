package io.github.g4lowy.order.domain.address

import io.github.g4lowy.order.domain.model.address.ZipCode
import zio.Scope
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

object ZipCodeSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("Zip code validation should")(
      test("fail when value is empty") {
        val zipCode = ZipCode.Unvalidated("")
        assertTrue(zipCode.validate.isInvalid)
      },
      test("fail when value is blank") {
        val zipCode = ZipCode.Unvalidated("   ")
        assertTrue(zipCode.validate.isInvalid)
      },
      test("fail when value does not match pattern") {
        val zipCode = ZipCode.Unvalidated("12 672")
        assertTrue(zipCode.validate.isInvalid)
      },
      test("succeed otherwise ") {
        val zipCode = ZipCode.Unvalidated("12-456")
        assertTrue(zipCode.validate.isValid)
      }
    )
}
