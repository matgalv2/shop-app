package io.github.g4lowy.customer.domain.model

import io.github.g4lowy.validation.validators.{ NotValidated, Validation }
import io.github.g4lowy.validation.validators.Validator._

import java.util.UUID

final case class CustomerId private(value: UUID)

object CustomerId {
  def generate: CustomerId.Unvalidated = CustomerId.Unvalidated(UUID.randomUUID.toString)

  def fromUUID(uuid: UUID) = CustomerId(uuid)

  final case class Unvalidated(value: String) extends NotValidated[CustomerId] {
    def validate: Validation[FailureDescription, CustomerId] =
      uuid.apply(value).map(uuid => CustomerId.apply(UUID.fromString(uuid)))

    def unsafeValidation: CustomerId = CustomerId(UUID.fromString(value))
  }
}
