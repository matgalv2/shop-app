package io.github.g4lowy.http.database

import com.zaxxer.hikari.{ HikariConfig, HikariDataSource }
import io.getquill.CamelCase
import io.getquill.jdbczio.Quill
import io.github.g4lowy.http.AppConfig
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import zio.{ ZIO, ZLayer }

import javax.sql.DataSource

object DatabaseConfiguration {

  val flyway: ZIO[DataSource with AppConfig, Nothing, MigrateResult] =
    for {
      config     <- ZIO.service[AppConfig]
      dataSource <- ZIO.service[DataSource]
      migrations =
        Flyway
          .configure()
          .dataSource(dataSource)
          .schemas(config.database.schema)
          .load()
          .migrate()
    } yield migrations

  val dataSource: ZIO[AppConfig, Nothing, HikariDataSource] =
    ZIO.serviceWith[AppConfig] { config =>
      val hikariConfig = new HikariConfig()
      hikariConfig.setJdbcUrl(config.database.url)
      hikariConfig.setUsername(config.database.username)
      hikariConfig.setPassword(config.database.password)
      hikariConfig.setSchema(config.database.schema)
      new HikariDataSource(hikariConfig)
    }

  val quillPostgres =
    ZLayer.fromZIO(dataSource).flatMap(ds => Quill.DataSource.fromDataSource(ds.get))

  val postgresLive = Quill.Postgres.fromNamingStrategy(CamelCase)

}
