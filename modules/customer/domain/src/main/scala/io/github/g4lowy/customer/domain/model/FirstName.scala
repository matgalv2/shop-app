package io.github.g4lowy.customer.domain.model

import io.github.g4lowy.validation.validators.{ NotValidated, Validation }
import io.github.g4lowy.validation.validators.Validator.FailureDescription
import io.github.g4lowy.validation.validators.Validator._

final case class FirstName private (value: String)

object FirstName {
  final case class Unvalidated(value: String) extends NotValidated[FirstName] {
    override def validate: Validation[FailureDescription, FirstName] =
      (nonEmpty and nonBlank and capitalized).apply(value).map(FirstName.apply)

    override def unsafeValidation: FirstName = FirstName.apply(value)
  }
}
