package io.github.g4lowy.http.service

import io.github.g4lowy.client.domain.model.{ Client, ClientError, ClientId }
import io.github.g4lowy.client.domain.repository.ClientRepository
import zio.{ URIO, ZIO }

object ClientService {

  def getClients: URIO[ClientRepository, List[Client]] =
    ClientRepository.getAll

  def getClientById(clientId: ClientId): ZIO[ClientRepository, ClientError.NotFound, Client] =
    ClientRepository.getById(clientId)

  def createClient(client: Client): URIO[ClientRepository, ClientId] = ClientRepository.create(client)

  def updateClient(clientId: ClientId, client: Client): ZIO[ClientRepository, ClientError.NotFound, Unit] =
    ClientRepository.update(clientId, client)

  def deleteClient(clientId: ClientId): ZIO[ClientRepository, ClientError.NotFound, Unit] =
    /*
      TODO: after implementing orders ensure that client that is going to be deleted, doesn't have active orders
     */
    ClientRepository.delete(clientId)
}
