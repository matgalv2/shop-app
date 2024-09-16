package io.github.g4lowy.client.model

import io.github.g4lowy.validation.extras.NotValidated
import io.github.g4lowy.validation.validators.Validation
import io.github.g4lowy.validation.validators.Validator._

import java.util.UUID

case class ClientId private(value: UUID)

object ClientId {
  def generate: ClientId.Unvalidated = ClientId.Unvalidated(UUID.randomUUID.toString)

  final case class Unvalidated(value: String) extends NotValidated[ClientId] {
    def validate: Validation[String, ClientId] = uuid.apply(value).map(uuid => ClientId.apply(UUID.fromString(uuid)))

    def unsafeValidation: ClientId = ClientId(UUID.fromString(value))
  }
}
