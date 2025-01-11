package io.github.g4lowy.http

import io.github.g4lowy.customer.infrastructure.repository.CustomerRepositoryPostgres
import io.github.g4lowy.http.cyclicjobs.OrderJobs
import io.github.g4lowy.http.database.DatabaseConfiguration
import io.github.g4lowy.http.database.DatabaseConfiguration.{postgresLive, quillDataSource}
import io.github.g4lowy.order.infrastructure.repository.OrderRepositoryPostgres
import io.github.g4lowy.product.infrastructure.repository.ProductRepositoryPostgres
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object ShopApp extends ZIOAppDefault {

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    for {
      _          <- DatabaseConfiguration.flyway
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
      HttpServer.live
    )
  private val dependencies =
    AppConfig.live >+> quillDataSource >+> postgresLive >+> CustomerRepositoryPostgres.live ++ ProductRepositoryPostgres.live ++ HttpServer.live

  private val handleBackground = {
    for {
      _ <- OrderJobs.archiveOrdersOnceADay.forkDaemon
//      _ <- ZIO.log(getMemoryStatistics).repeat(Schedule.fixed(5.minutes)).forkDaemon
    } yield ()
  }

//  private def getMemoryStatistics = {
//    val runtime = Runtime.getRuntime
//
//    val `2^30`: Double = 1024 * 1024 * 1024
//
//    s"Current RAM usage ${runtime.totalMemory() / `2^30`}GB / ${runtime.maxMemory() / `2^30`}GB"
//  }

  /*
   TODO:
    1. find a way to expose special services from one module to another
    2. add order mapper tests
    3. ----add "ARCHIVED" order status and make cyclic task that archives orders finished 3 months ago from now---
    4. add tests ^^^
    5. add triggers to database (update order status to move archived orders to different table)
   */
}
