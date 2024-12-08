package io.github.g4lowy.order.domain.address

import io.github.g4lowy.order.domain.model.address.Country
import zio.Scope
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

object CountrySpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("Country validation should")(
      test("fail when value is empty") {
        val country = Country.Unvalidated("")
        assertTrue(country.validate.isInvalid)
      },
      test("fail when value is blank") {
        val country = Country.Unvalidated("   ")
        assertTrue(country.validate.isInvalid)
      },
      test("fail when value is not capitalized") {
        val country = Country.Unvalidated("ann")
        assertTrue(country.validate.isInvalid)
      },
      test("succeed otherwise ") {
        val country = Country.Unvalidated("Ann")
        assertTrue(country.validate.isValid)
      }
    )
}
