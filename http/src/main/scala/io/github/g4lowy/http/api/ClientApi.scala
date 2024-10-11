package io.github.g4lowy.http.api

import http.generated.clients.{
  ClientsHandler,
  ClientsResource,
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
import zio.{ RIO, Runtime, ZIO }
import io.github.g4lowy.validation.extras._
import io.github.g4lowy.http.converters.clients._
import io.github.g4lowy.http.error._
import io.github.g4lowy.error.ErrorMessage._
import io.github.g4lowy.http.AppEnvironment
import org.http4s.HttpRoutes

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
    ClientService
      .getClientById(ClientId.fromUUID(clientId))
      .mapBoth(error => respond.NotFound(ErrorResponse.single(error.toMessage)), _.toAPI)
      .map(respond.Ok)
      .merge

  override def createClient(
    respond: CreateClientResponse.type
  )(body: CreateClient): RIO[Environment, CreateClientResponse] =
    ZIO
      .fromNotValidated(body.toDomain)
      .mapError(error => respond.BadRequest(ErrorResponse.single(error.toMessage)))
      .flatMap(ClientService.createClient)
      .map(clientId => respond.Created(clientId.toAPI))
      .merge

  override def updateClient(
    respond: UpdateClientResponse.type
  )(clientId: UUID, body: UpdateClient): RIO[Environment, UpdateClientResponse] =
    ZIO
      .fromNotValidated(body.toDomain)
      .mapError(error => respond.BadRequest(ErrorResponse.single(error.toMessage)))
      .flatMap(client =>
        ClientService
          .updateClient(ClientId.fromUUID(clientId), client)
          .mapError(error => respond.NotFound(ErrorResponse.single(error.toMessage)))
      )
      .as(respond.NoContent)
      .merge

  override def deleteClient(
    respond: DeleteClientResponse.type
  )(clientId: UUID): RIO[Environment, DeleteClientResponse] =
    ClientService
      .deleteClient(ClientId.fromUUID(clientId))
      .mapBoth(error => respond.NotFound(ErrorResponse.single(error.toMessage)), _ => respond.NoContent)
      .merge

}
object ClientApi {
  type Environment = ClientRepository

  val routes: ZIO[AppEnvironment, Nothing, HttpRoutes[RIO[AppEnvironment, *]]] = {
    import zio.interop.catz._

    ZIO
      .runtime[AppEnvironment]
      .map { implicit r: Runtime[AppEnvironment] =>
        new ClientsResource[RIO[AppEnvironment, *]]
      }
      .map(_.routes(new ClientApi()))
  }
}
