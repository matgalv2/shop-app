package io.github.g4lowy.customer.domain

import io.github.g4lowy.customer.domain.model.LastName
import zio.Scope
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault}
import zio.test._

object LastNameSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("LastName validation should")(
      test("fail when value is empty") {
        val lastName = LastName.Unvalidated("")
        assertTrue(lastName.validate.isInvalid)
      },
      test("fail when value is blank") {
        val lastName = LastName.Unvalidated("   ")
        assertTrue(lastName.validate.isInvalid)
      },
      test("fail when value is not capitalized") {
        val lastName = LastName.Unvalidated("ann")
        assertTrue(lastName.validate.isInvalid)
      },
      test("succeed otherwise ") {
        val lastName = LastName.Unvalidated("Ann")
        assertTrue(lastName.validate.isValid)
      }
    )
}
