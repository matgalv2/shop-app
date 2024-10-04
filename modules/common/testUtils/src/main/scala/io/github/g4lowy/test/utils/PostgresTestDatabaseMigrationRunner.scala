package io.github.g4lowy.test.utils

import io.github.g4lowy.test.utils.TestDatabaseConfiguration.{ dataSource, flyway }
import zio.{ Scope, ZIO, ZIOAppArgs, ZIOAppDefault }

object PostgresTestDatabaseMigrationRunner extends ZIOAppDefault {

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    flyway.provide(AppTestConfig.testConfigLive, dataSource)

}
