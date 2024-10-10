package io.github.g4lowy.test.utils

import io.github.g4lowy.test.utils.TestDatabaseConfiguration.{ quillDataSource, flyway }
import zio.{ Scope, ZIO, ZIOAppArgs, ZIOAppDefault }

object PostgresAcceptanceTestDatabaseMigrationRunner extends ZIOAppDefault {

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    flyway.provide(AppTestConfig.acceptanceTestConfigLive, quillDataSource)

}
