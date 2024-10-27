package io.github.g4lowy.http

import io.github.g4lowy.customer.infrastructure.repository.CustomerRepositoryPostgres
import io.github.g4lowy.http.database.DatabaseConfiguration
import io.github.g4lowy.http.database.DatabaseConfiguration.{ postgresLive, quillDataSource }
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
      HttpServer.live
    )

  private val dependencies =
    AppConfig.live >+> quillDataSource >+> postgresLive >+> CustomerRepositoryPostgres.live ++ ProductRepositoryPostgres.live ++ HttpServer.live

  /*
    TODO:
      1. Rename entity Client to Customer
      2. Rename table OrdersProducts to OrdersDetails
   */
}
