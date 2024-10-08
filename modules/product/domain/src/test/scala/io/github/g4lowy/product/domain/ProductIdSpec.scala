package io.github.g4lowy.product.domain

import io.github.g4lowy.product.domain.model.ProductId
import zio.Scope
import zio.test.{ ZIOSpecDefault, _ }

object ProductIdSpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ClientId validation should")(
      test("fail when value is not uuid string") {
        val id = ProductId.Unvalidated(" ")
        assertTrue(id.validate.isInvalid)
      },
      test("succeed when values is uuid string") {
        val id = ProductId.Unvalidated("f3567d10-1100-4ede-a8d9-2befa28c993e")
        assertTrue(id.validate.isValid)
      }
    )
}
