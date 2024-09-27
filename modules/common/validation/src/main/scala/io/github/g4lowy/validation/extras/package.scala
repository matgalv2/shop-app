package io.github.g4lowy.validation

import io.github.g4lowy.validation.validators.{ NotValidated, Validation, Validator }
import zio._

package object extras {

  implicit class ZIOValidationOps(private val zio: ZIO.type) extends AnyVal {

    def fromValidation[A, E](validation: Validation[E, A]): IO[E, A] =
      ZIO.fromEither(validation.toEither)

    def fromNotValidated[A](notValidated: NotValidated[A]): IO[Validator.FailureDescription, A] =
      fromValidation(notValidated.validate)

    def fromNotValidated[A](notValidated: Option[NotValidated[A]]): IO[Validator.FailureDescription, Option[A]] =
      notValidated.map(_.validate).map(fromValidation).map(_.map(Some.apply)).getOrElse(ZIO.none)
  }
}
