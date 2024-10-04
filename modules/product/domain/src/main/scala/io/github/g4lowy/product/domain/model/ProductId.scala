package io.github.g4lowy.product.domain.model

import io.github.g4lowy.validation.validators.{ NotValidated, Validation }
import io.github.g4lowy.validation.validators.Validator._

import java.util.UUID

final case class ProductId private (value: UUID)

object ProductId {

  def generate: ProductId.Unvalidated = ProductId.Unvalidated(UUID.randomUUID.toString)

  def fromUUID(uuid: UUID) = ProductId.Unvalidated(uuid.toString)

  final case class Unvalidated(value: String) extends NotValidated[ProductId] {

    override def validate: Validation[FailureDescription, ProductId] =
      uuid.apply(value).map(uuid => ProductId.apply(UUID.fromString(uuid)))

    override def unsafeValidation: ProductId = ProductId(UUID.fromString(value))
  }
}
