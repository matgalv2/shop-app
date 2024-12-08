package io.github.g4lowy.order.domain.model.address

import io.github.g4lowy.validation.validators.Validator.{FailureDescription, capitalized, nonBlank, nonEmpty}
import io.github.g4lowy.validation.validators.{NotValidated, Validation}

final case class City private (value: String)

object City {

  final case class Unvalidated(value: String) extends NotValidated[City] {

    override def validate: Validation[FailureDescription, City] =
      (nonEmpty and nonBlank and capitalized).apply(value).map(City.apply)

    override protected def unsafeValidation: City = City.apply(value)

    private[address] def validateUnsafe: City = unsafeValidation
  }
}
