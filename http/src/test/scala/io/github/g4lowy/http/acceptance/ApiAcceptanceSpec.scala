package io.github.g4lowy.http.acceptance

import io.circe.Json
import io.getquill.CamelCase
import io.getquill.jdbczio.Quill
import io.github.g4lowy.customer.infrastructure.repository.CustomerRepositoryPostgres
import io.github.g4lowy.http.AppEnvironment
import io.github.g4lowy.order.infrastructure.repository.OrderRepositoryPostgres
import io.github.g4lowy.product.infrastructure.repository.ProductRepositoryPostgres
import io.github.g4lowy.test.utils.TestDatabaseConfiguration.postgresLive
import io.github.g4lowy.test.utils.{AppTestConfig, TestDatabaseConfiguration}
import org.http4s.circe._
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.{HttpRoutes, Request, Response}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, GivenWhenThen}
import zio.interop.catz._
import zio.{RIO, Runtime, URIO, Unsafe, ZIO, ZLayer}

abstract class ApiAcceptanceSpec
    extends AnyWordSpec
    with GivenWhenThen
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach {

  val routes: ZIO[AppEnvironment, Nothing, HttpRoutes[RIO[AppEnvironment, *]]]

  def cleanData: URIO[Quill.Postgres[CamelCase], Unit]

  override def beforeAll: Unit = runEffect(cleanData.provide(dependencies))

  override def afterEach: Unit = runEffect(cleanData.provide(dependencies))

  private def runEffect[E, A](effect: ZIO[Any, E, A]): A =
    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe
        .run(effect)
        .getOrThrowFiberFailure()
    }

  private val dataSource = runEffect(
    TestDatabaseConfiguration.dataSource.provide(AppTestConfig.acceptanceTestConfigLive)
  )
  private val dependencies =
    ZLayer.succeed(
      dataSource
    ) >+> postgresLive >+> CustomerRepositoryPostgres.live ++ ProductRepositoryPostgres.live ++ OrderRepositoryPostgres.live

  protected def handleRequest(request: Request[RIO[AppEnvironment, *]]): Response[RIO[AppEnvironment, *]] =
    runEffect {
      routes
        .flatMap(_.orNotFound(request))
        .provide(dependencies)
    }

  protected def mapResponseBodyToJson(response: Response[RIO[AppEnvironment, *]]): Json =
    runEffect(
      response
        .as[Json]
        .provide(dependencies)
    )

}
