package io.github.g4lowy.http

import io.github.g4lowy.client.infrastructure.repository.ClientRepositoryPostgres
import io.github.g4lowy.http.AppConfig.configLive
import io.github.g4lowy.http.database.DatabaseConfiguration
import io.github.g4lowy.http.database.DatabaseConfiguration.{ postgresLive, quillDataSource }
import io.github.g4lowy.product.infrastructure.repository.ProductRepositoryPostgres
import zio.{ Scope, ZIO, ZIOAppArgs, ZIOAppDefault }

object ShopApp extends ZIOAppDefault {

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    for {
      _      <- DatabaseConfiguration.flyway
      server <- Controller.server
    } yield server
  }
    .provide(
      configLive,
      quillDataSource,
      postgresLive,
      ClientRepositoryPostgres.live,
      ProductRepositoryPostgres.live,
      HttpServer.live
    )

  private val dependencies =
    configLive >+> quillDataSource >+> postgresLive >+> ClientRepositoryPostgres.live ++ ProductRepositoryPostgres.live ++ HttpServer.live

}
