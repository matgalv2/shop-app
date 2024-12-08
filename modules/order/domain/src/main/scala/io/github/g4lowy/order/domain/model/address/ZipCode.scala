package io.github.g4lowy.order.domain.model.address

import io.github.g4lowy.validation.validators.Validator.{FailureDescription, matchesRegex}
import io.github.g4lowy.validation.validators.{NotValidated, Validation}

final case class ZipCode private (value: String)

object ZipCode {

  final case class Unvalidated(value: String) extends NotValidated[ZipCode] {

    override def validate: Validation[FailureDescription, ZipCode] =
      matchesRegex("^\\d{2}-\\d{3}$".r).apply(value).map(ZipCode.apply)

    override def unsafeValidation: ZipCode = ZipCode.apply(value)

    private[address] def validateUnsafe: ZipCode = unsafeValidation

  }
}
