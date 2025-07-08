package io.github.g4lowy.http

import io.github.g4lowy.customer.infrastructure.repository.CustomerRepositoryPostgres
import io.github.g4lowy.http.cyclicjobs.OrderJobs
import io.github.g4lowy.http.database.DatabaseUtils
import io.github.g4lowy.http.database.DatabaseUtils.{postgresLive, quillDataSource}
import io.github.g4lowy.order.application.OrderProcessor
import io.github.g4lowy.order.infrastructure.broker.{KafkaOrderRequestConsumer, KafkaOrderRequestProducer, KafkaOrderResponseProducer}
import io.github.g4lowy.order.infrastructure.database.repository.OrderRepositoryPostgres
import io.github.g4lowy.product.infrastructure.repository.ProductRepositoryPostgres
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object ShopApp extends ZIOAppDefault {

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    for {
      _          <- DatabaseUtils.runFlywayMigrations
      _          <- Controller.httpServer
      _          <- handleBackground
      useForever <- ZIO.never
    } yield useForever
  }
    .provide(
      AppConfig.live,
      quillDataSource,
      postgresLive,
      CustomerRepositoryPostgres.live,
      ProductRepositoryPostgres.live,
      OrderRepositoryPostgres.live,
      HttpServer.live,
      KafkaOrderRequestProducer.toLayer,
      KafkaOrderResponseProducer.toLayer,
      KafkaOrderRequestConsumer.toLayer,
      Scope.default
    )

  private val dependencies =
    AppConfig.live >+> quillDataSource >+> postgresLive >+> CustomerRepositoryPostgres.live ++ ProductRepositoryPostgres.live ++ HttpServer.live

  private val handleBackground = {
    for {
      _ <- OrderJobs.archiveOrdersOnceADay.forkDaemon
      _ <- OrderProcessor.consumeRequests.forkScoped
    } yield ()
  }

  /*
   TODO:
    1. add test for archiving method
    2. OrderRepository utilizes ProductError - refactor
   */
}
