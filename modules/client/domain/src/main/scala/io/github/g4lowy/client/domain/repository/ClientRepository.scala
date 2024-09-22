package io.github.g4lowy.client.domain.repository

import io.github.g4lowy.client.domain.model.{Client, ClientError, ClientId}
import zio.{IO, UIO}
import zio.macros.accessible

@accessible
trait ClientRepository {
  def getById(clientId: ClientId): IO[ClientError.NotFound, Client]
  def getAll: UIO[List[Client]]
  def create(client: Client): UIO[ClientId]
  def update(clientId: ClientId, client: Client): IO[ClientError.NotFound, Unit]
  def delete(clientId: ClientId): UIO[Unit]
}
