package io.github.g4lowy.product.domain

import io.github.g4lowy.product.domain.model.Price
import zio.Scope
import zio.test._

object PriceSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("Price validation should")(
      test("fail when value is 0") {
        val price = Price.Unvalidated(0d)
        assertTrue(price.validate.isInvalid)
      },
      test("fail when value is negative") {
        val price = Price.Unvalidated(-1d)
        assertTrue(price.validate.isInvalid)
      },
      test("succeed when value is positive") {
        val price = Price.Unvalidated(1d)
        assertTrue(price.validate.isValid)
      }
    )
}
