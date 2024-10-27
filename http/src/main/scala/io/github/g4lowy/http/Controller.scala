package io.github.g4lowy.http

import io.github.g4lowy.http.api.{ CustomerApi, ProductApi }
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Server
import org.http4s.HttpRoutes
import zio.{ &, RIO, ZIO }
import zio.interop.catz._

object Controller {

  private def combineRoutes[R](route: HttpRoutes[RIO[R, *]], routes: HttpRoutes[RIO[R, *]]*): HttpRoutes[RIO[R, *]] = {
    import cats.syntax.all._
    routes.fold(route)(_ <+> _)
  }

  private val combinedRoutes =
    for {
      customersRoutes <- CustomerApi.routes
      productsRoutes  <- ProductApi.routes
      combinedRoutes = combineRoutes(customersRoutes, productsRoutes)
    } yield combinedRoutes.orNotFound

  val httpServer: ZIO[AppEnvironment & HttpServer, Throwable, Server[RIO[AppEnvironment, *]]] =
    for {
      routes <- combinedRoutes
      server <- HttpServer.bindServer(routes)
      host   <- HttpServer.host
      port   <- HttpServer.port
      _      <- ZIO.log(f"Starting server at $host:$port")
    } yield server

}
