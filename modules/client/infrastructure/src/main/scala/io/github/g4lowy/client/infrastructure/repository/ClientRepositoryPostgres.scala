//package io.github.g4lowy.client.infrastructure.repository
//
//
//import io.getquill
//import io.getquill.{CamelCase, PostgresJdbcContext}
//import io.github.g4lowy.client.domain.model.{Client, ClientError, ClientId}
//import io.github.g4lowy.client.domain.repository.ClientRepository
//import io.github.g4lowy.client.infrastructure.model.ClientSQL
//import zio.{IO, UIO, ZIO}
//
//case class ClientRepositoryPostgres(ctx: PostgresJdbcContext[CamelCase]) extends ClientRepository{
//
//  import ctx._
//
//  private val clients = quote {
//    querySchema[ClientSQL]("Clients")
//  }
//
//  override def create(client: Client): UIO[ClientId] = {
//    val result =
//      run{
//        quote {
//          clients.insertValue(lift(ClientSQL.fromDomain(client)))
//        }
//      }
//
//    result
//      .map(_ => client.clientId)
//      .orDieWith(x => x)
//  }
//
//  override def getById(clientId: ClientId): IO[ClientError.NotFound, Client] = {
//    val result =
//      run {
//        quote{
//          clients.filter(_.clientId == lift(clientId.value))
//        }
//      }
//      result
//      .map(_.headOption.map(_.toDomain()))
//      .some
//      .orElseFail(ClientError.NotFound(clientId))
//  }
//
//  override def getAll: UIO[List[Client]] = {
//    val result = run(quote(clients))
//
//    result
//      .map(_.map(_.toDomain()))
//      .orDieWith(x => x)
//  }
//
//  override def update(clientId: ClientId, client: Client): IO[ClientError.NotFound, Unit] = {
//    val result = run{
//      quote{
//        clients
//          .filter(_.clientId == lift(clientId.value))
//          .update(
//            _.firstName -> lift(client.firstName.value),
//            _.lastName -> lift(client.lastName.value),
//            _.phone -> lift(client.phone.value)
//          )
//      }
//    }
//
//    result
//      .orDieWith(x => x)
//      .flatMap( rowsNo =>
//        if (rowsNo == 1) ZIO.unit
//        else  ZIO.fail(ClientError.NotFound(clientId))
//      )
//  }
//
//  override def delete(clientId: ClientId): UIO[Unit] = {
//    val result = run{
//      quote{
//        clients
//          .filter(_.clientId == lift(clientId.value))
//          .delete
//      }
//    }
//
//    result
//      .flatMap( rowsNo =>
//        if (rowsNo == 1) ZIO.unit
//        else  ZIO.fail(ClientError.NotFound(clientId))
//      )
//      .orDieWith(x => new Exception(""))
//  }
//
//
//
//
//}
//
//object ClientRepositoryPostgres {
//
//}
