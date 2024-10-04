package io.github.g4lowy.product.domain.model

import io.github.g4lowy.validation.validators.Validator._
import io.github.g4lowy.validation.validators.{ NotValidated, Validation }

final case class Price private (value: Double)

object Price {

  final case class Unvalidated(value: Double) extends NotValidated[Price] {

    override def validate: Validation[FailureDescription, Price] =
      positive.apply(value).map(Price.apply)

    override def unsafeValidation: Price = Price.apply(value)
  }
}
