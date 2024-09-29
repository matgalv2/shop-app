package io.github.g4lowy.client.domain.model

import io.github.g4lowy.validation.validators.{ NotValidated, Validation }
import io.github.g4lowy.validation.validators.Validator.{ matchesRegex, FailureDescription }

case class Phone private (value: String)

object Phone {
  final case class Unvalidated(number: String) extends NotValidated[Phone] {
    override def validate: Validation[FailureDescription, Phone] =
      matchesRegex("^\\+[0-9]{1,3}-[0-9]{6,}$".r).apply(number).map(Phone.apply)

    override def unsafeValidation: Phone = Phone.apply(number)
  }
}
