package io.github.g4lowy.order.domain.model

import io.github.g4lowy.validation.validators.{ NotValidated, Validation }
import io.github.g4lowy.validation.validators.Validator.{ uuid, FailureDescription }

import java.util.UUID

final case class OrderId private (value: UUID)

object OrderId {
  def generate: OrderId.Unvalidated = OrderId.Unvalidated(UUID.randomUUID.toString)

  def fromUUID(uuid: UUID) = OrderId(uuid)

  final case class Unvalidated(value: String) extends NotValidated[OrderId] {
    def validate: Validation[FailureDescription, OrderId] =
      uuid.apply(value).map(uuid => OrderId.apply(UUID.fromString(uuid)))

    def unsafeValidation: OrderId = OrderId(UUID.fromString(value))
  }
}
