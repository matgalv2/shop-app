package io.github.g4lowy.customer.domain.model

import io.github.g4lowy.validation.validators.{ NotValidated, Validation }
import io.github.g4lowy.validation.validators.Validator.{ matchesRegex, FailureDescription }

case class Phone private (value: String)

object Phone {
  final case class Unvalidated(value: String) extends NotValidated[Phone] {
    override def validate: Validation[FailureDescription, Phone] =
      matchesRegex("^\\+[0-9]{1,3}-[0-9]{6,}$".r).apply(value).map(Phone.apply)

    override def unsafeValidation: Phone = Phone.apply(value)
  }
}
