package io.github.g4lowy.customer.infrastructure.model

import io.github.g4lowy.customer.domain.model._

import java.sql.Date
import java.time.LocalDateTime
import java.util.UUID

case class CustomerSQL(
  customerId: UUID,
  firstName: String,
  lastName: String,
  birthDate: Option[Date],
  phone: String,
  createdAt: LocalDateTime
) {
  def toDomain: Customer.Unvalidated =
    Customer
      .Unvalidated(
        customerId = CustomerId.fromUUID(customerId),
        firstName  = FirstName.Unvalidated(firstName),
        lastName   = LastName.Unvalidated(lastName),
        birthDate  = birthDate,
        phone      = Phone.Unvalidated(phone),
        createdAt  = createdAt
      )
}

object CustomerSQL {

  def fromDomain(client: Customer): CustomerSQL =
    CustomerSQL(
      customerId = client.customerId.value,
      firstName  = client.firstName.value,
      lastName   = client.lastName.value,
      birthDate  = client.birthDate,
      phone      = client.phone.value,
      createdAt  = client.createdAt
    )
}
