package io.github.g4lowy.order.domain.model.address

import io.github.g4lowy.validation.validators.Validator.{FailureDescription, capitalized, nonBlank, nonEmpty}
import io.github.g4lowy.validation.validators.{NotValidated, Validation}

final case class Country private (value: String)

object Country {

  final case class Unvalidated(value: String) extends NotValidated[Country] {

    override def validate: Validation[FailureDescription, Country] =
      (nonEmpty and nonBlank and capitalized).apply(value).map(Country.apply)

    override protected def unsafeValidation: Country = Country.apply(value)

    private[address] def validateUnsafe: Country = unsafeValidation
  }
}
