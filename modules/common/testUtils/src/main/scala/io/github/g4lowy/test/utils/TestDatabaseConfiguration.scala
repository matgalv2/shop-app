package io.github.g4lowy.test.utils

import com.zaxxer.hikari.{ HikariConfig, HikariDataSource }
import io.getquill.CamelCase
import io.getquill.jdbczio.Quill
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import zio.{ ZIO, ZLayer }

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

  private val dataSourceHikari: ZIO[AppTestConfig, Nothing, HikariDataSource] =
    ZIO.serviceWith[AppTestConfig] { config =>
      val hikariConfig = new HikariConfig()
      hikariConfig.setJdbcUrl(config.database.url)
      hikariConfig.setUsername(config.database.username)
      hikariConfig.setPassword(config.database.password)
      hikariConfig.setSchema(config.database.schema)
      new HikariDataSource(hikariConfig)
    }

  val dataSource: ZLayer[AppTestConfig, Throwable, DataSource] =
    ZLayer.fromZIO(dataSourceHikari).flatMap(ds => Quill.DataSource.fromDataSource(ds.get))

  val postgresLive: ZLayer[DataSource, Nothing, Quill.Postgres[CamelCase.type]] =
    Quill.Postgres.fromNamingStrategy(CamelCase)
}
