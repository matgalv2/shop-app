package io.github.g4lowy.http

import io.github.g4lowy.customer.infrastructure.repository.CustomerRepositoryPostgres
import io.github.g4lowy.http.database.DatabaseConfiguration
import io.github.g4lowy.http.database.DatabaseConfiguration.{ postgresLive, quillDataSource }
import io.github.g4lowy.order.infrastructure.repository.OrderRepositoryPostgres
import io.github.g4lowy.product.infrastructure.repository.ProductRepositoryPostgres
import zio.{ Scope, ZIO, ZIOAppArgs, ZIOAppDefault }

object ShopApp extends ZIOAppDefault {

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    for {
      _          <- DatabaseConfiguration.flyway
      _          <- Controller.httpServer
      useForever <- ZIO.never
    } yield useForever
  }
    .provide(
      AppConfig.live,
      quillDataSource,
      postgresLive,
      CustomerRepositoryPostgres.live,
      ProductRepositoryPostgres.live,
      OrderRepositoryPostgres.live,
      HttpServer.live
    )

  private val dependencies =
    AppConfig.live >+> quillDataSource >+> postgresLive >+> CustomerRepositoryPostgres.live ++ ProductRepositoryPostgres.live ++ HttpServer.live

  /*

   TODO:
    1. add contact info to order
    2. add createdAt field to order
    3. implement pagination for all entities
    4. if mapping requires logic map api to dto and use dto in service
   */
}
