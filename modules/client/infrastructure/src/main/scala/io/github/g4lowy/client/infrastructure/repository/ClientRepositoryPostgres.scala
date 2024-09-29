package io.github.g4lowy.client.infrastructure.repository

import io.getquill.CamelCase
import io.getquill.jdbczio.Quill
import io.github.g4lowy.client.domain.model.{ Client, ClientError, ClientId }
import io.github.g4lowy.client.domain.repository.ClientRepository
import io.github.g4lowy.client.infrastructure.model.ClientSQL
import io.github.g4lowy.error.DatabaseCriticalFailure
import zio.{ IO, UIO, URLayer, ZIO, ZLayer }

case class ClientRepositoryPostgres(quill: Quill.Postgres[CamelCase]) extends ClientRepository {

  import quill._

  private val clients = quote {
    querySchema[ClientSQL]("Clients")
  }

  override def create(client: Client): UIO[ClientId] = {
    val result =
      run {
        quote {
          clients.insertValue(lift(ClientSQL.fromDomain(client)))
        }
      }

    result
      .as(client.clientId)
      .orDieWith(error => DatabaseCriticalFailure(error.getMessage))
  }

  override def getById(clientId: ClientId): IO[ClientError.NotFound, Client] = {
    val result =
      run {
        quote {
          clients.filter(_.clientId == lift(clientId.value))
        }
      }
    result
      .orDieWith(error => DatabaseCriticalFailure(error.getMessage))
      .flatMap(_.headOption match {
        case Some(value) => ZIO.succeed(value.toDomain())
        case None        => ZIO.fail(ClientError.NotFound(clientId))
      })
  }

  override def getAll: UIO[List[Client]] = {
    val result = run(quote(clients))

    result
      .map(_.map(_.toDomain()))
      .orDieWith(fail => DatabaseCriticalFailure(fail.getMessage))
  }

  override def update(clientId: ClientId, client: Client): IO[ClientError.NotFound, Unit] = {
    val result = run {
      quote {
        clients
          .filter(_.clientId == lift(clientId.value))
          .update(
            _.firstName -> lift(client.firstName.value),
            _.lastName -> lift(client.lastName.value),
            _.phone -> lift(client.phone.value)
          )
      }
    }

    result
      .orDieWith(error => DatabaseCriticalFailure(error.getMessage))
      .flatMap(rowsNo => ZIO.unless(rowsNo == 1)(ZIO.fail(ClientError.NotFound(clientId))))
      .unit
  }

  override def delete(clientId: ClientId): IO[ClientError.NotFound, Unit] = {
    val result = run {
      quote {
        clients
          .filter(_.clientId == lift(clientId.value))
          .delete
      }
    }

    result
      .orDieWith(error => DatabaseCriticalFailure(error.getMessage))
      .flatMap(rowsNo => ZIO.fail(ClientError.NotFound(clientId)).unless(rowsNo == 1))
      .unit
  }
}

object ClientRepositoryPostgres {
  val live: URLayer[Quill.Postgres[CamelCase], ClientRepositoryPostgres] =
    ZLayer.fromFunction(ClientRepositoryPostgres.apply _)
}
