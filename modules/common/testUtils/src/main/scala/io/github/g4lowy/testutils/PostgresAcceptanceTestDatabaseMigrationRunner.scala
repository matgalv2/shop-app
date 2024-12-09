package io.github.g4lowy.testutils

import io.github.g4lowy.testutils.TestDatabaseConfiguration.{dataSourceLive, flyway}
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object PostgresAcceptanceTestDatabaseMigrationRunner extends ZIOAppDefault {

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    flyway.provide(AppTestConfig.acceptanceTestConfigLive, dataSourceLive)

}
