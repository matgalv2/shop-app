package io.github.g4lowy.testutils

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.getquill.CamelCase
import io.getquill.jdbczio.Quill
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import zio.{ZIO, ZLayer}

import javax.sql.DataSource

object TestDatabaseConfiguration {

  val flyway: ZIO[DataSource with AppTestConfig, Nothing, MigrateResult] =
    for {
      config     <- ZIO.service[AppTestConfig]
      dataSource <- ZIO.service[DataSource]
      migrations =
        Flyway
          .configure()
          .locations("filesystem:http/src/main/resources/db/migration")
          .dataSource(dataSource)
          .schemas(config.database.schema)
          .load()
          .migrate()
    } yield migrations

  private def createHikariDataSource(config: AppTestConfig) = {
    val hikariConfig = new HikariConfig()
    hikariConfig.setJdbcUrl(config.database.url)
    hikariConfig.setUsername(config.database.username)
    hikariConfig.setPassword(config.database.password)
    hikariConfig.setSchema(config.database.schema)
    new HikariDataSource(hikariConfig)
  }

  val dataSource: ZIO[AppTestConfig, Throwable, DataSource] =
    ZIO.serviceWith[AppTestConfig](config => createHikariDataSource(config))

  val dataSourceLive: ZLayer[AppTestConfig, Throwable, DataSource] = ZLayer.fromZIO(dataSource)

  val postgresLive: ZLayer[DataSource, Nothing, Quill.Postgres[CamelCase.type]] =
    Quill.Postgres.fromNamingStrategy(CamelCase)
}
