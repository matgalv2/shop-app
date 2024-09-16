package io.github.g4lowy.client.model

import io.github.g4lowy.validation.extras.NotValidated
import io.github.g4lowy.validation.validators.Validation
import io.github.g4lowy.validation.validators.Validator.FailureDescription

import java.time.LocalDateTime

case class Client private(clientId: ClientId, firstName: FirstName, lastName: LastName, address: Address,
                          phone: Phone, createdAt: LocalDateTime)

object Client{

  final case class Unvalidated(clientId: ClientId.Unvalidated,
                               firstName: FirstName.Unvalidated,
                               lastName: LastName.Unvalidated,
                               address: Address.Unvalidated,
                               phone: Phone.Unvalidated,
                               createdAt: LocalDateTime) extends NotValidated[Client] {

    override def validate: Validation[FailureDescription, Client] =
      for {
        id <- clientId.validate
        name <- firstName.validate
        surname <- lastName.validate
        address <- address.validate
        phone <- phone.validate
      } yield Client(id, name, surname, address, phone, createdAt)

    override def unsafeValidation: Client =
      Client(
        clientId  = clientId.unsafeValidation,
        firstName = firstName.unsafeValidation,
        lastName  = lastName.unsafeValidation,
        address   = address.unsafeValidation,
        phone     = phone.unsafeValidation,
        createdAt = createdAt
      )
  }
}
