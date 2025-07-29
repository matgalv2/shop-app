package io.github.g4lowy.http

import io.github.g4lowy.customer.infrastructure.repository.CustomerRepositoryPostgres
import io.github.g4lowy.http.broker.BrokerUtils
import io.github.g4lowy.http.cyclicjobs.OrderJobs
import io.github.g4lowy.http.database.DatabaseUtils
import io.github.g4lowy.order.application.OrderProcessor
import io.github.g4lowy.order.infrastructure.database.repository.OrderRepositoryPostgres
import io.github.g4lowy.product.infrastructure.repository.ProductRepositoryPostgres
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object ShopApp extends ZIOAppDefault {

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    for {
      _          <- DatabaseUtils.runFlywayMigrations
      _          <- Controller.startHttpServer
      _          <- backgroundTasks
      useForever <- ZIO.never
    } yield useForever
  }
    .provide(
      AppConfig.live,
      DatabaseUtils.quillDataSource,
      DatabaseUtils.postgresLive,
      CustomerRepositoryPostgres.live,
      ProductRepositoryPostgres.live,
      OrderRepositoryPostgres.live,
      HttpServer.live,
      BrokerUtils.kafkaBootstrapServers,
      BrokerUtils.kafkaProducersLive,
      BrokerUtils.kafkaConsumersLive,
      Scope.default
    )

  private val dependencies =
    AppConfig.live >+> DatabaseUtils.quillDataSource >+> DatabaseUtils.postgresLive >+> CustomerRepositoryPostgres.live ++ ProductRepositoryPostgres.live ++ HttpServer.live

  private val backgroundTasks = {
    for {
      _ <- OrderJobs.archiveOrdersOnceADay.forkDaemon
      _ <- OrderProcessor.consumeRequests.forkScoped
    } yield ()
  }
}
