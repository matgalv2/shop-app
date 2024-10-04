package io.github.g4lowy.product.domain.model

import io.github.g4lowy.validation.validators.Validator._
import io.github.g4lowy.validation.validators.{ NotValidated, Validation }

final case class Name private (value: String)

object Name {

  final case class Unvalidated(value: String) extends NotValidated[Name] {

    override def validate: Validation[FailureDescription, Name] =
      (nonEmpty and nonBlank).apply(value).map(Name.apply)

    override def unsafeValidation: Name = Name.apply(value)
  }
}
