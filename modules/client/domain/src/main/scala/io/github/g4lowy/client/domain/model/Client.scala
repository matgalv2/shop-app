package io.github.g4lowy.client.domain.model

import io.github.g4lowy.validation.validators.{ NotValidated, Validation, Validator }
import io.github.g4lowy.validation.validators.Validator.{ alwaysValid, FailureDescription }

import java.sql.Date
import java.time.LocalDateTime

final case class Client private (
  clientId: ClientId,
  firstName: FirstName,
  lastName: LastName,
  birthDate: Option[Date],
  phone: Phone,
  createdAt: LocalDateTime
)

object Client {

  final case class Unvalidated(
    clientId: ClientId.Unvalidated,
    firstName: FirstName.Unvalidated,
    lastName: LastName.Unvalidated,
    birthDate: Option[Date],
    phone: Phone.Unvalidated,
    createdAt: LocalDateTime
  ) extends NotValidated[Client] {

    override def validate: Validation[FailureDescription, Client] =
      for {
        id        <- clientId.validate
        name      <- firstName.validate
        surname   <- lastName.validate
        birthDate <- Validator.opt(alwaysValid)(birthDate)
        phone     <- phone.validate
      } yield Client(id, name, surname, birthDate, phone, createdAt)

    override def unsafeValidation: Client =
      Client(
        clientId  = clientId.unsafeValidation,
        firstName = firstName.unsafeValidation,
        lastName  = lastName.unsafeValidation,
        birthDate = birthDate,
        phone     = phone.unsafeValidation,
        createdAt = createdAt
      )
  }
}
