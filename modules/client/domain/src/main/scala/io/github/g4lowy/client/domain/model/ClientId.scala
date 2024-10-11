package io.github.g4lowy.client.domain.model

import io.github.g4lowy.validation.validators.{ NotValidated, Validation }
import io.github.g4lowy.validation.validators.Validator._

import java.util.UUID

final case class ClientId private (value: UUID)

object ClientId {
  def generate: ClientId.Unvalidated = ClientId.Unvalidated(UUID.randomUUID.toString)

  def fromUUID(uuid: UUID) = ClientId(uuid)

  final case class Unvalidated(value: String) extends NotValidated[ClientId] {
    def validate: Validation[FailureDescription, ClientId] =
      uuid.apply(value).map(uuid => ClientId.apply(UUID.fromString(uuid)))

    def unsafeValidation: ClientId = ClientId(UUID.fromString(value))
  }
}
