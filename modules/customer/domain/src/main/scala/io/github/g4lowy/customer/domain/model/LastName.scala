package io.github.g4lowy.customer.domain.model

import io.github.g4lowy.validation.validators.Validator._
import io.github.g4lowy.validation.validators.{NotValidated, Validation}

final case class LastName private (value: String)

object LastName {
  final case class Unvalidated(value: String) extends NotValidated[LastName] {
    override def validate: Validation[FailureDescription, LastName] =
      (nonEmpty and nonBlank and capitalized).apply(value).map(LastName.apply)

    override protected def unsafeValidation: LastName = LastName.apply(value)

    private[customer] def validateUnsafe: LastName = unsafeValidation
  }
}
