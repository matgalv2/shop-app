package io.github.g4lowy.client.model

import io.github.g4lowy.validation.extras.NotValidated
import io.github.g4lowy.validation.validators.Validation
import io.github.g4lowy.validation.validators.Validator.FailureDescription
import io.github.g4lowy.validation.validators.Validator._

case class LastName private(value: String)

object LastName{
  final case class Unvalidated(value: String) extends NotValidated[LastName]{
    override def validate: Validation[FailureDescription, LastName] =
      (nonEmpty and nonBlank and capitalized).apply(value).map(LastName.apply)

    override def unsafeValidation: LastName = LastName.apply(value)
  }
}
