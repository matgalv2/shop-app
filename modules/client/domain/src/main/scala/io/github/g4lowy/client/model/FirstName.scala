package io.github.g4lowy.client.model

import io.github.g4lowy.validation.extras.NotValidated
import io.github.g4lowy.validation.validators.Validation
import io.github.g4lowy.validation.validators.Validator.FailureDescription
import io.github.g4lowy.validation.validators.Validator._

case class FirstName private(value: String)

object FirstName{
  final case class Unvalidated(value: String) extends NotValidated[FirstName]{
    override def validate: Validation[FailureDescription, FirstName] =
      (nonEmpty and nonBlank and capitalized).apply(value).map(FirstName.apply)

    override def unsafeValidation: FirstName = FirstName.apply(value)
  }
}
