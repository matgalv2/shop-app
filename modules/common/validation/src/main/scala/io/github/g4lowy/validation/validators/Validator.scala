package io.github.g4lowy.validation.validators

import io.github.g4lowy.validation.validators.Validation.{ Invalid, Valid }

import java.sql.Date
import java.util.UUID
import scala.util.Try
import scala.util.matching.Regex

class Validator[A](val func: A => Boolean, val description: Validator.FailureDescription)
    extends ValidationRunner[ValidationRunner.Id, A] {

  override def validator: Validator[A] = this

  private val chain: (A => Boolean, A => Boolean) => ((Boolean, Boolean) => Boolean) => A => Boolean = (fun1, fun2) =>
    operator =>
      x => {
        operator(fun1(x), fun2(x))
      }

  def and: Validator[A] => Validator[A] = validator => {
    Validator(chain(func, validator.func)(_ && _), s"($description and ${validator.description})")
  }

  def or: Validator[A] => Validator[A] = validator => {
    Validator(chain(func, validator.func)(_ || _), s"($description or ${validator.description})")
  }

  def unary_! : Validator[A] =
    Validator(arg => !func(arg), s"not ($description)")

  override def run(value: A): Validation[Validator.FailureDescription, A] =
    if (func(value)) Valid(value) else Invalid(description)

  def optional(option: Option[A]): Validation[Validator.FailureDescription, Option[A]] =
    ValidationRunner.ForOption(this).run(option)

  def list(list: List[A]): Validation[Validator.FailureDescription, List[A]] = ValidationRunner.ForList(this).run(list)

}

object Validator {

  type FailureDescription = String

  def apply[A](func: A => Boolean, description: FailureDescription): Validator[A] = new Validator(func, description)

  def alwaysValid[A] = new Validator[A](_ => true, "always valid")

  def failed[A] = new Validator[A](_ => false, s"failed ")

  val nonBlank: Validator[String] = new Validator(x => !x.isBlank, s"non blank")

  val nonEmpty: Validator[String] = new Validator(x => x.nonEmpty, "non empty")

  val maxLength: Int => Validator[String] = length => new Validator(x => x.length <= length, s"max length: $length")

  val minLength: Int => Validator[String] = length => new Validator(x => x.length >= length, s"min length: $length")

  val matchesRegex: Regex => Validator[String] = regex => new Validator(x => regex.matches(x), s"matches regex: $regex")

  val digitsOnly: Validator[String] = new Validator(string => string.matches("^[0-9]+$"), s"digits only")

  val capitalized: Validator[String] =
    new Validator(string => if (string.isEmpty) false else string.head.isUpper, s"capitalized ")

  val contains: String => Validator[String] = substring =>
    new Validator(string => string.contains(substring), s"contains: $substring")

  val uuid: Validator[String] =
    new Validator(string => Try(UUID.fromString(string)).isSuccess, s"incorrect uuid format")

  val positive: Validator[Double] = new Validator(_ > 0, "value needs to be positive")

  val negative: Validator[Double] = new Validator(_ < 0, "value needs to be negative")

  val date: Validator[String] = new Validator(string => Try(Date.valueOf(string)).isSuccess, "incorrect date format")

  def min(value: Int): Validator[Int] = new Validator(_ >= value, s"value needs to be greater than or equal to $value")

  def max(value: Int): Validator[Int] = new Validator(_ <= value, s"value needs to be lesser than or equal to $value")

  def min(value: Double): Validator[Double] =
    new Validator(_ >= value, s"value needs to be greater than or equal to $value")

  def max(value: Double): Validator[Double] =
    new Validator(_ <= value, s"value needs to be lesser than or equal to $value")

  def inRange(range: Range): Validator[Int] =
    new Validator(value => range.contains(value), s"value needs to be in range $range")

  def opt[A](validator: Validator[A])(value: Option[A]): Validation[FailureDescription, Option[A]] = value match {
    case None        => Valid(None)
    case Some(value) => validator.run(value).map(Some.apply)
  }

  def validOrCheck[A, B <: NotValidated[A]](option: Option[B]): Validation[FailureDescription, Option[A]] =
    option match {
      case Some(value) => value.validate.map(Some.apply)
      case None        => Valid(None)
    }

  def validateIterable[A, B <: NotValidated[A]](list: Iterable[B]): Validation[FailureDescription, Iterable[A]] =
    Validation.collect(list.map(_.validate))(_ + _)

}
