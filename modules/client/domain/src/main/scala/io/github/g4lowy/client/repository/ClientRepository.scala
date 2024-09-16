package io.github.g4lowy.client.repository

import io.github.g4lowy.client.model.{Client, ClientError, ClientId}
import zio.{IO, UIO}
import zio.macros.accessible

@accessible
trait ClientRepository {
  def getById(clientId: ClientId): UIO[Option[Client]]
  def getAll: UIO[List[Client]]
  def create(client: Client.Unvalidated): UIO[ClientId]
  def update(clientId: ClientId, client: Client.Unvalidated): IO[ClientError.NotFound, Unit]
  def delete(clientId: ClientId): UIO[Unit]
}
