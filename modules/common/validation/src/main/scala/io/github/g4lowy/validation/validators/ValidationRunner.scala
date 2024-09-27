package io.github.g4lowy.validation.validators

import io.github.g4lowy.validation.validators.Validation.{ Invalid, Valid }

trait ValidationRunner[F[_], A] {

  def validator: Validator[A]

  def run(value: F[A]): Validation[Validator.FailureDescription, F[A]]

  final def apply(value: F[A]): Validation[Validator.FailureDescription, F[A]] = run(value)

}

object ValidationRunner {
  type Id[A] = A

  final case class ForOption[A](_validator: Validator[A]) extends ValidationRunner[Option, A] {
    override def validator: Validator[A] = _validator

    override def run(value: Option[A]): Validation[Validator.FailureDescription, Option[A]] =
      value match {
        case None => Valid(Option.empty[A])
        case Some(wrappedValue) =>
          validator.run(wrappedValue) match {
            case Invalid(error)    => Invalid(error)
            case Valid(validValue) => Valid(Some(validValue))
          }
      }
  }

  final case class ForList[A](_validator: Validator[A]) extends ValidationRunner[List, A] {
    override def validator: Validator[A] = _validator

    override def run(value: List[A]): Validation[Validator.FailureDescription, List[A]] = {
      val mapped = value.map(x => validator.run(x))
      mapped match {
        case _ if mapped.count(x => x.isValid) >= 0 => Validation.Valid(value)
        case _                                      => mapped.filter(x => x.isInvalid).head.asInstanceOf[Validation[Validator.FailureDescription, List[A]]]
      }
    }
  }
}
