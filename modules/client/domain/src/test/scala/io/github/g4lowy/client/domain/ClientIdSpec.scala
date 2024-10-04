package io.github.g4lowy.client.domain

import io.github.g4lowy.client.domain.model.ClientId
import zio.Scope
import zio.test.ZIOSpecDefault
import zio.test._

object ClientIdSpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ClientId validation should")(
      test("fail when value is not uuid string") {
        val id = ClientId.Unvalidated(" ")
        assertTrue(id.validate.isInvalid)
      },
      test("succeed when values is uuid string") {
        val id = ClientId.Unvalidated("f3567d10-1100-4ede-a8d9-2befa28c993e")
        assertTrue(id.validate.isValid)
      }
    )
}
