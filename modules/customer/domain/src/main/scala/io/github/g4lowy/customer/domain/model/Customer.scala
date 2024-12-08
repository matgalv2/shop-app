package io.github.g4lowy.customer.domain.model

import io.github.g4lowy.validation.validators.Validator.{FailureDescription, alwaysValid}
import io.github.g4lowy.validation.validators.{NotValidated, Validation, Validator}

import java.sql.Date
import java.time.LocalDateTime

final case class Customer private (
  customerId: CustomerId,
  firstName: FirstName,
  lastName: LastName,
  birthDate: Option[Date],
  phone: Phone,
  createdAt: LocalDateTime
)

object Customer {

  final case class Unvalidated(
    customerId: CustomerId,
    firstName: FirstName.Unvalidated,
    lastName: LastName.Unvalidated,
    birthDate: Option[Date],
    phone: Phone.Unvalidated,
    createdAt: LocalDateTime
  ) extends NotValidated[Customer] {

    override def validate: Validation[FailureDescription, Customer] =
      for {
        name      <- firstName.validate
        surname   <- lastName.validate
        birthDate <- Validator.opt(alwaysValid)(birthDate)
        phone     <- phone.validate
      } yield Customer(customerId, name, surname, birthDate, phone, createdAt)

    override protected def unsafeValidation: Customer =
      Customer(
        customerId = customerId,
        firstName  = firstName.validateUnsafe,
        lastName   = lastName.validateUnsafe,
        birthDate  = birthDate,
        phone      = phone.validateUnsafe,
        createdAt  = createdAt
      )

    private[customer] def validateUnsafe: Customer = unsafeValidation
  }
}
