package io.github.g4lowy.customer.domain

import io.github.g4lowy.customer.domain.model.Phone
import zio.Scope
import zio.test._

object PhoneSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("Phone validation should")(
      test("fail when value does not match pattern") {
        val phone = Phone.Unvalidated("48123456789")
        assertTrue(phone.validate.isInvalid)
      },
      test("fail when value does not match pattern") {
        val phone = Phone.Unvalidated("+48123456789")
        assertTrue(phone.validate.isInvalid)
      },
      test("fail when value does not match pattern") {
        val phone = Phone.Unvalidated("+48-1234")
        assertTrue(phone.validate.isInvalid)
      },
      test("succeed otherwise ") {
        val phone = Phone.Unvalidated("+48-123456")
        assertTrue(phone.validate.isValid)
      }
    )
}
