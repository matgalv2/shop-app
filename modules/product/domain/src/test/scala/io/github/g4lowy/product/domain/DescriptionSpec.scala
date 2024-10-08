package io.github.g4lowy.product.domain

import io.github.g4lowy.product.domain.model.Description
import zio.Scope
import zio.test._

object DescriptionSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("Description validation should")(
      test("fail when value is empty") {
        val description = Description.Unvalidated("")
        assertTrue(description.validate.isInvalid)
      },
      test("fail when value is blank") {
        val description = Description.Unvalidated("   ")
        assertTrue(description.validate.isInvalid)
      },
      test("succeed otherwise") {
        val description = Description.Unvalidated("some description")
        assertTrue(description.validate.isValid)
      }
    )
}
