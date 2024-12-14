package io.github.g4lowy.http.acceptance

import io.circe.Json
import io.getquill.CamelCase
import io.getquill.jdbczio.Quill
import io.github.g4lowy.customer.infrastructure.repository.CustomerRepositoryPostgres
import io.github.g4lowy.http.AppEnvironment
import io.github.g4lowy.order.infrastructure.repository.OrderRepositoryPostgres
import io.github.g4lowy.product.infrastructure.repository.ProductRepositoryPostgres
import io.github.g4lowy.testutils.TestDatabaseConfiguration.postgresLive
import io.github.g4lowy.testutils.{AppTestConfig, TestDatabaseConfiguration}
import org.http4s.circe._
import org.http4s.implicits.{http4sKleisliResponseSyntaxOptionT, http4sLiteralsSyntax}
import org.http4s.{HttpRoutes, Request, Response, Uri}
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

  protected val routes: URIO[AppEnvironment, HttpRoutes[RIO[AppEnvironment, *]]]

  protected def cleanData: URIO[Quill.Postgres[CamelCase], Unit]

  protected val nonExistentId: String = "99999999-9999-9999-9999-2a035d9e16ba"

  protected val uriFromString: String => Uri = (url: String) => Uri.fromString(url).getOrElse(uri"")

  override protected def beforeAll: Unit = runEffect(cleanData.provide(dependencies))

  override protected def afterEach: Unit = runEffect(cleanData.provide(dependencies))

  protected def cleanOtherData: URIO[Quill.Postgres[CamelCase], Unit] = ZIO.unit

  override protected def afterAll: Unit = runEffect(cleanOtherData.provide(dependencies))

  protected def runEffect[E, A](effect: ZIO[Any, E, A]): A =
    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe
        .run(effect)
        .getOrThrowFiberFailure()
    }

  private val dataSource = runEffect(
    TestDatabaseConfiguration.dataSource.provide(AppTestConfig.acceptanceTestConfigLive)
  )
  protected val dependencies =
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
