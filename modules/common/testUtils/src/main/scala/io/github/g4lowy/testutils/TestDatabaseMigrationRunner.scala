package io.github.g4lowy.testutils

import io.github.g4lowy.testutils.TestDatabaseConfiguration.{dataSourceLive, flyway}
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object TestDatabaseMigrationRunner extends ZIOAppDefault {

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    for {
      _ <- ZIO.log("Starting migrations for integration tests database")
      _ <- flyway.provide(AppTestConfig.integrationTestConfigLive, dataSourceLive)
      _ <- ZIO.log("Starting migrations for acceptance tests database")
      _ <- flyway.provide(AppTestConfig.acceptanceTestConfigLive, dataSourceLive)
    } yield ()

}
