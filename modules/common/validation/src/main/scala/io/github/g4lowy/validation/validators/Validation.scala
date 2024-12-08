package io.github.g4lowy.validation.validators

import io.github.g4lowy.validation.validators.Validation.{Invalid, Valid}

sealed trait Validation[+E, +A] { self =>

  def map[B](func: A => B): Validation[E, B] =
    self match {
      case Valid(value)   => Valid(func(value))
      case Invalid(error) => Invalid(error)
    }

  def flatMap[E1 >: E, B](func: A => Validation[E1, B]): Validation[E1, B] =
    self match {
      case Valid(value)   => func(value)
      case Invalid(error) => Invalid(error)
    }

  def isValid: Boolean = self match {
    case Valid(_)   => true
    case Invalid(_) => false
  }

  def isInvalid: Boolean = !self.isValid

  def toEither: Either[E, A] = self match {
    case Valid(value)   => Right(value)
    case Invalid(error) => Left(error)
  }

  def exists(p: A => Boolean): Boolean =
    self match {
      case Valid(value) => p(value)
      case Invalid(_)   => false
    }
}

object Validation {
  final case class Valid[A](value: A) extends Validation[Nothing, A]
  final case class Invalid[E](error: E) extends Validation[E, Nothing]

  def collect[A, E](iterable: Iterable[Validation[E, A]])(op: (E, E) => E): Validation[E, Iterable[A]] = {
    val empty: Validation[E, List[A]] = Valid(List.empty[A])
    iterable.foldLeft(empty) { case (acc, element) =>
      (acc, element) match {
        case (Valid(x), Valid(y))     => Valid(y :: x)
        case (Invalid(x), Valid(_))   => Invalid(x)
        case (Valid(_), Invalid(y))   => Invalid(y)
        case (Invalid(x), Invalid(y)) => Invalid(op(x, y))
      }
    }
  }
}
