package io.github.g4lowy.http

import io.github.g4lowy.http.api.{ ClientApi, ProductApi }
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.HttpRoutes
import zio.{ &, RIO, ZIO }
import zio.interop.catz._

object Controller {

  def combineRoutes[R](route: HttpRoutes[RIO[R, *]], routes: HttpRoutes[RIO[R, *]]*): HttpRoutes[RIO[R, *]] = {
    import cats.syntax.all._
    routes.fold(route)(_ <+> _)
  }

  private val combinedRoutes =
    for {
      clientsRoutes  <- ClientApi.routes
      productsRoutes <- ProductApi.routes
      combinedRoutes = combineRoutes(clientsRoutes, productsRoutes)
    } yield combinedRoutes.orNotFound

  val server: ZIO[AppEnvironment & HttpServer, Throwable, Nothing] =
    for {
      combinedRoutes <- combinedRoutes
      binding        <- HttpServer.bindServer(combinedRoutes)
      _              <- ZIO.log(f"Starting server at ${binding.address}")
      useForever     <- ZIO.never
    } yield useForever

}
