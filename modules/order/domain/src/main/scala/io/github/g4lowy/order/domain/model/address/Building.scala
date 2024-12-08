package io.github.g4lowy.order.domain.model.address

import io.github.g4lowy.validation.validators.Validator.{FailureDescription, matchesRegex}
import io.github.g4lowy.validation.validators.{NotValidated, Validation}

final case class Building private (value: String)

object Building {

  final case class Unvalidated(value: String) extends NotValidated[Building] {

    override def validate: Validation[FailureDescription, Building] =
      matchesRegex("^[0-9]*[A-z]*$".r).apply(value).map(Building.apply)

    override protected def unsafeValidation: Building = Building.apply(value)

    private[address] def validateUnsafe: Building = unsafeValidation
  }
}
