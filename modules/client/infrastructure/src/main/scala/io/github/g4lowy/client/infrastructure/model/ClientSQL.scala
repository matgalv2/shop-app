package io.github.g4lowy.client.infrastructure.model

import io.github.g4lowy.client.domain.model.{ Client, ClientId, FirstName, LastName, Phone }

import java.sql.Date
import java.time.LocalDateTime
import java.util.UUID

case class ClientSQL(
  clientId: UUID,
  firstName: String,
  lastName: String,
  birthDate: Option[Date],
  phone: String,
  createdAt: LocalDateTime
) {
  def toDomain: Client =
    Client
      .Unvalidated(
        clientId  = ClientId.fromUUID(clientId),
        firstName = FirstName.Unvalidated(firstName),
        lastName  = LastName.Unvalidated(lastName),
        birthDate = birthDate,
        phone     = Phone.Unvalidated(phone),
        createdAt = createdAt
      )
      .unsafeValidation
}

object ClientSQL {

  def fromDomain(client: Client): ClientSQL =
    ClientSQL(
      clientId  = client.clientId.value,
      firstName = client.firstName.value,
      lastName  = client.lastName.value,
      birthDate = client.birthDate,
      phone     = client.phone.value,
      createdAt = client.createdAt
    )
}
