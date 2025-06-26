package io.github.g4lowy.abstracttype

import io.github.g4lowy.validation.validators.Validator.{FailureDescription, capitalized, nonBlank, nonEmpty}
import io.github.g4lowy.validation.validators.{NotValidated, Validation}

case class CapitalizedString protected (value: String)

object CapitalizedString {
  final class Unvalidated(value: String) extends NotValidated[CapitalizedString] {

    override def validate: Validation[FailureDescription, CapitalizedString] =
      (nonEmpty and nonBlank and capitalized).apply(value).map(CapitalizedString.apply)

    override def unsafeValidation: CapitalizedString = CapitalizedString.apply(value)
  }
}
