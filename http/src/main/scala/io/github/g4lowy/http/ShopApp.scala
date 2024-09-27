package io.github.g4lowy.http

import io.github.g4lowy.client.infrastructure.repository.ClientRepositoryPostgres
import io.github.g4lowy.http.AppConfig.configLive
import io.github.g4lowy.http.database.DatabaseConfiguration
import io.github.g4lowy.http.database.DatabaseConfiguration.{ postgresLive, quillPostgres }

import zio.{ Scope, ZIO, ZIOAppArgs, ZIOAppDefault }

object ShopApp extends ZIOAppDefault {

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    for {
      migrations <- DatabaseConfiguration.flyway
      config     <- ZIO.service[AppConfig]
      _          <- ZIO.log(config.http.host.toString)
    } yield migrations
  }
    .provideLayer(dependencies)

  private val dependencies = configLive >+> quillPostgres >+> postgresLive >+> ClientRepositoryPostgres.live

}
