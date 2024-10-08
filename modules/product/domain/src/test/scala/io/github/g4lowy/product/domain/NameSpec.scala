package io.github.g4lowy.product.domain

import io.github.g4lowy.product.domain.model.Name
import zio.Scope
import zio.test._

object NameSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("Name validation should")(
      test("fail when value is empty") {
        val name = Name.Unvalidated("")
        assertTrue(name.validate.isInvalid)
      },
      test("fail when value is blank") {
        val name = Name.Unvalidated("   ")
        assertTrue(name.validate.isInvalid)
      },
      test("succeed otherwise ") {
        val name = Name.Unvalidated("Ann")
        assertTrue(name.validate.isValid)
      }
    )
}
