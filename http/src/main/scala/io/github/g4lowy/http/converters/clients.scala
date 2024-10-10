package io.github.g4lowy.http.converters

import http.generated.definitions.{ CreateClient, GetClient, UpdateClient }
import io.github.g4lowy.client.domain.model.{ Client, ClientId, FirstName, LastName, Phone }
import io.github.g4lowy.client.domain
import io.scalaland.chimney.dsl._

import java.sql.Date
import java.time.LocalDateTime

object clients {

  implicit class CreateClientOps(private val create: CreateClient) extends AnyVal {
    def toDomain: Client.Unvalidated =
      create
        .into[Client.Unvalidated]
        .withFieldConst(_.clientId, ClientId.generate)
        .withFieldComputed(_.firstName, x => FirstName.Unvalidated(x.firstName))
        .withFieldComputed(_.lastName, x => LastName.Unvalidated(x.lastName))
        .withFieldComputed(_.birthDate, _.birthDate.map(value => Date.valueOf(value)))
        .withFieldComputed(_.phone, x => Phone.Unvalidated(x.phone))
        .withFieldConst(_.createdAt, LocalDateTime.now())
        .transform
  }

  implicit class UpdateClientOps(private val update: UpdateClient) extends AnyVal {
    def toDomain: Client.Unvalidated =
      update
        .into[Client.Unvalidated]
        .withFieldConst(_.clientId, ClientId.generate)
        .withFieldComputed(_.firstName, x => FirstName.Unvalidated(x.firstName))
        .withFieldComputed(_.lastName, x => LastName.Unvalidated(x.lastName))
        .withFieldComputed(_.birthDate, _.birthDate.map(value => Date.valueOf(value)))
        .withFieldComputed(_.phone, x => Phone.Unvalidated(x.phone))
        .withFieldConst(_.createdAt, LocalDateTime.now())
        .transform
  }

  implicit class ClientOps(private val client: Client) extends AnyVal {
    def toAPI: GetClient =
      client
        .into[GetClient]
        .withFieldComputed(_.clientId, _.clientId.value)
        .withFieldComputed(_.firstName, _.firstName.value)
        .withFieldComputed(_.lastName, _.lastName.value)
        .withFieldComputed(_.birthDate, _.birthDate.map(_.toLocalDate))
        .withFieldComputed(_.phone, _.phone.value)
        .transform

  }

  implicit class ClientIdOps(private val clientId: domain.model.ClientId) extends AnyVal {
    def toAPI: http.generated.definitions.ClientId =
      clientId
        .into[http.generated.definitions.ClientId]
        .withFieldRenamed(_.value, _.clientId)
        .transform
  }
}
