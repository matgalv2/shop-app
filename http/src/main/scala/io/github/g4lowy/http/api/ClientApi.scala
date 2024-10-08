package io.github.g4lowy.http.api

import http.generated.clients.{
  ClientsHandler,
  CreateClientResponse,
  DeleteClientResponse,
  GetAllClientsResponse,
  GetClientByIdResponse,
  UpdateClientResponse
}
import http.generated.definitions.{ CreateClient, ErrorResponse, UpdateClient }
import io.github.g4lowy.client.domain.model.ClientId
import io.github.g4lowy.client.domain.repository.ClientRepository
import io.github.g4lowy.http.api.ClientApi.Environment
import io.github.g4lowy.http.service.ClientService
import zio.{ RIO, ZIO }
import io.github.g4lowy.validation.extras._
import io.github.g4lowy.http.converters.clients._
import io.github.g4lowy.http.error._
import io.github.g4lowy.error.ErrorMessage._
import io.github.g4lowy.http.AppEnvironment

import java.util.UUID

class ClientApi extends ClientsHandler[RIO[AppEnvironment, *]] {

  override def getAllClients(respond: GetAllClientsResponse.type)(): RIO[Environment, GetAllClientsResponse] =
    ClientService.getClients
      .map(_.map(_.toAPI))
      .map(_.toVector)
      .map(respond.Ok)

  override def getClientById(
    respond: GetClientByIdResponse.type
  )(clientId: UUID): RIO[Environment, GetClientByIdResponse] =
    ZIO
      .fromNotValidated(ClientId.fromUUID(clientId))
      .mapError(error => respond.BadRequest(ErrorResponse.single(error.toMessage)))
      .flatMap { clientId =>
        ClientService
          .getClientById(clientId)
          .mapError(error => respond.NotFound(ErrorResponse.single(error.toMessage)))
      }
      .map(_.toAPI)
      .map(respond.Ok)
      .merge

  override def createClient(
    respond: CreateClientResponse.type
  )(body: CreateClient): RIO[Environment, CreateClientResponse] =
    ZIO
      .fromNotValidated(body.toDomain)
      .mapError(error => respond.BadRequest(ErrorResponse.single(error.toMessage)))
      .flatMap(ClientService.createClient)
      .map(clientId => respond.Created(clientId.value))
      .merge

  override def updateClient(
    respond: UpdateClientResponse.type
  )(clientId: UUID, body: UpdateClient): RIO[Environment, UpdateClientResponse] =
    ZIO
      .fromNotValidated(body.toDomain)
      .mapError(error => respond.BadRequest(ErrorResponse.single(error.toMessage)))
      .flatMap(client =>
        ZIO
          .fromNotValidated(ClientId.fromUUID(clientId))
          .mapError(error => respond.BadRequest(ErrorResponse.single(error.toMessage)))
          .flatMap(id =>
            ClientService
              .updateClient(id, client)
              .mapError(error => respond.NotFound(ErrorResponse.single(error.toMessage)))
          )
      )
      .as(respond.NoContent)
      .merge

  override def deleteClient(
    respond: DeleteClientResponse.type
  )(clientId: UUID): RIO[Environment, DeleteClientResponse] =
    ZIO
      .fromNotValidated(ClientId.fromUUID(clientId))
      .mapError(error => respond.BadRequest(ErrorResponse.single(error.toMessage)))
      .flatMap { id =>
        ClientService
          .deleteClient(id)
          .mapError(error => respond.NotFound(ErrorResponse.single(error.toMessage)))
      }
      .as(respond.NoContent)
      .merge

}
object ClientApi {
  type Environment = ClientRepository
}
