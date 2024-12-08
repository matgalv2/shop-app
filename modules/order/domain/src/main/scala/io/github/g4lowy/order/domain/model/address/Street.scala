package io.github.g4lowy.order.domain.model.address

import io.github.g4lowy.validation.validators.Validator.{FailureDescription, capitalized, nonBlank, nonEmpty}
import io.github.g4lowy.validation.validators.{NotValidated, Validation}

final case class Street private (value: String)

object Street {

  final case class Unvalidated(value: String) extends NotValidated[Street] {
    override def validate: Validation[FailureDescription, Street] =
      (nonEmpty and nonBlank and capitalized).apply(value).map(Street.apply)

    override protected def unsafeValidation: Street = Street.apply(value)

    private[address] def validateUnsafe: Street = unsafeValidation
  }
}
