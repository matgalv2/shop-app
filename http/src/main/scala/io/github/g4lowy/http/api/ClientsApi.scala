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
import http.generated.definitions.{ CreateClient, UpdateClient }
import io.github.g4lowy.client.infrastructure.repository.ClientRepositoryPostgres
import io.github.g4lowy.http.api.ClientsApi.Environment
import zio.RIO

import java.util.UUID

class ClientsApi extends ClientsHandler[RIO[ClientsApi.Environment, *]] {

  override def createClient(respond: CreateClientResponse.type)(
    body: CreateClient
  ): RIO[Environment, CreateClientResponse] = ???

  override def deleteClient(respond: DeleteClientResponse.type)(
    clientId: UUID
  ): RIO[Environment, DeleteClientResponse] = ???

  override def getAllClients(respond: GetAllClientsResponse.type)(): RIO[Environment, GetAllClientsResponse] = ???

  override def getClientById(respond: GetClientByIdResponse.type)(
    clientId: UUID
  ): RIO[Environment, GetClientByIdResponse] = ???

  override def updateClient(
    respond: UpdateClientResponse.type
  )(clientId: UUID, body: UpdateClient): RIO[Environment, UpdateClientResponse] = ???
}
object ClientsApi {
  type Environment = ClientRepositoryPostgres
}
