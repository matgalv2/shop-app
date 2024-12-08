package io.github.g4lowy.product.domain.model

import io.github.g4lowy.validation.validators.Validator._
import io.github.g4lowy.validation.validators.{NotValidated, Validation}

final case class Price private (value: BigDecimal)

object Price {

  final case class Unvalidated(value: BigDecimal) extends NotValidated[Price] {

    override def validate: Validation[FailureDescription, Price] =
      greaterThan(BigDecimal(0)).apply(value).map(Price.apply)

    override protected def unsafeValidation: Price = Price.apply(value)

    private[product] def validateUnsafe: Price = unsafeValidation
  }
}
