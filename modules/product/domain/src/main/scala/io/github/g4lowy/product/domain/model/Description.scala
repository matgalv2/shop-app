package io.github.g4lowy.product.domain.model

import io.github.g4lowy.validation.validators.Validator._
import io.github.g4lowy.validation.validators.{ NotValidated, Validation }

final case class Description private (value: String)

object Description {

  final case class Unvalidated(value: String) extends NotValidated[Description] {

    override def validate: Validation[FailureDescription, Description] =
      (nonEmpty and nonBlank).apply(value).map(Description.apply)

    override def unsafeValidation: Description = Description.apply(value)
  }
}
