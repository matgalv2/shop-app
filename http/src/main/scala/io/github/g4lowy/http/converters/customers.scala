package io.github.g4lowy.http.converters

import http.generated.definitions.{ CreateCustomer, GetCustomer, UpdateCustomer }
import io.github.g4lowy.customer.domain.model.{ Customer, CustomerId, FirstName, LastName, Phone }
import io.scalaland.chimney.dsl._

import java.sql.Date
import java.time.LocalDateTime

object customers {

  implicit class CreateClientOps(private val create: CreateCustomer) extends AnyVal {
    def toDomain: Customer.Unvalidated =
      create
        .into[Customer.Unvalidated]
        .withFieldConst(_.customerId, CustomerId.generate)
        .withFieldComputed(_.firstName, x => FirstName.Unvalidated(x.firstName))
        .withFieldComputed(_.lastName, x => LastName.Unvalidated(x.lastName))
        .withFieldComputed(_.birthDate, _.birthDate.map(value => Date.valueOf(value)))
        .withFieldComputed(_.phone, x => Phone.Unvalidated(x.phone))
        .withFieldConst(_.createdAt, LocalDateTime.now())
        .transform
  }

  implicit class UpdateClientOps(private val update: UpdateCustomer) extends AnyVal {
    def toDomain: Customer.Unvalidated =
      update
        .into[Customer.Unvalidated]
        .withFieldConst(_.customerId, CustomerId.generate)
        .withFieldComputed(_.firstName, x => FirstName.Unvalidated(x.firstName))
        .withFieldComputed(_.lastName, x => LastName.Unvalidated(x.lastName))
        .withFieldComputed(_.birthDate, _.birthDate.map(value => Date.valueOf(value)))
        .withFieldComputed(_.phone, x => Phone.Unvalidated(x.phone))
        .withFieldConst(_.createdAt, LocalDateTime.now())
        .transform
  }

  implicit class ClientOps(private val client: Customer) extends AnyVal {
    def toAPI: GetCustomer =
      client
        .into[GetCustomer]
        .withFieldComputed(_.customerId, _.customerId.value)
        .withFieldComputed(_.firstName, _.firstName.value)
        .withFieldComputed(_.lastName, _.lastName.value)
        .withFieldComputed(_.birthDate, _.birthDate.map(_.toLocalDate))
        .withFieldComputed(_.phone, _.phone.value)
        .transform

  }

  implicit class ClientIdOps(private val clientId: CustomerId) extends AnyVal {

    def toAPI: http.generated.definitions.CustomerId =
      clientId
        .into[http.generated.definitions.CustomerId]
        .withFieldRenamed(_.value, _.value)
        .transform
  }
}
