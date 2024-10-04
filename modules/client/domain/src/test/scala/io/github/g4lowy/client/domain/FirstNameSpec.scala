package io.github.g4lowy.client.domain

import io.github.g4lowy.client.domain.model.FirstName
import zio.Scope
import zio.test.{ Spec, TestEnvironment, ZIOSpecDefault }
import zio.test._

object FirstNameSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("FirstName validation should")(
      test("fail when value is empty") {
        val firstName = FirstName.Unvalidated("")
        assertTrue(firstName.validate.isInvalid)
      },
      test("fail when value is blank") {
        val firstName = FirstName.Unvalidated("   ")
        assertTrue(firstName.validate.isInvalid)
      },
      test("fail when value is not capitalized") {
        val firstName = FirstName.Unvalidated("ann")
        assertTrue(firstName.validate.isInvalid)
      },
      test("succeed otherwise ") {
        val firstName = FirstName.Unvalidated("Ann")
        assertTrue(firstName.validate.isValid)
      }
    )
}
