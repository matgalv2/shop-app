package io.github.g4lowy.order.domain.model.address

import io.github.g4lowy.validation.validators.Validator.{FailureDescription, matchesRegex}
import io.github.g4lowy.validation.validators.{NotValidated, Validation}

final case class Apartment private (value: String)
object Apartment {

  final case class Unvalidated(value: String) extends NotValidated[Apartment] {

    override def validate: Validation[FailureDescription, Apartment] =
      matchesRegex("^\\d{1,5}$".r).apply(value).map(Apartment.apply)

    override protected def unsafeValidation: Apartment = Apartment.apply(value)

    private[address] def validateUnsafe: Apartment = unsafeValidation
  }
}
