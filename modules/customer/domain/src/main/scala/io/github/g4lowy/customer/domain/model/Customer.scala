package io.github.g4lowy.customer.domain.model

import io.github.g4lowy.validation.validators.{ NotValidated, Validation, Validator }
import io.github.g4lowy.validation.validators.Validator.{ alwaysValid, FailureDescription }

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
                                customerId: CustomerId.Unvalidated,
                                firstName: FirstName.Unvalidated,
                                lastName: LastName.Unvalidated,
                                birthDate: Option[Date],
                                phone: Phone.Unvalidated,
                                createdAt: LocalDateTime
  ) extends NotValidated[Customer] {

    override def validate: Validation[FailureDescription, Customer] =
      for {
        id        <- customerId.validate
        name      <- firstName.validate
        surname   <- lastName.validate
        birthDate <- Validator.opt(alwaysValid)(birthDate)
        phone     <- phone.validate
      } yield Customer(id, name, surname, birthDate, phone, createdAt)

    override def unsafeValidation: Customer =
      Customer(
        customerId = customerId.unsafeValidation,
        firstName  = firstName.unsafeValidation,
        lastName   = lastName.unsafeValidation,
        birthDate  = birthDate,
        phone      = phone.unsafeValidation,
        createdAt  = createdAt
      )
  }
}
