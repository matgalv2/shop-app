package io.github.g4lowy.http

import cats.data.Kleisli
import cats.~>
import http.generated.clients.ClientsResource
import http.generated.products.ProductsResource
import io.github.g4lowy.http.api.ProductApi.Environment
import io.github.g4lowy.http.api.{ ClientApi, ProductApi }
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.{ HttpRoutes, Request, Response }
import zio.{ &, RIO, Runtime, ZEnvironment, ZIO, ZLayer }

object Controller {

  val makeClientsResource: RIO[AppEnvironment, ClientsResource[RIO[AppEnvironment, *]]] = {
    import zio.interop.catz._
    ZIO.runtime[AppEnvironment].map { implicit r: Runtime[AppEnvironment] =>
      new ClientsResource[RIO[AppEnvironment, *]]
    }
  }

  private val clientsHandler = new ClientApi()

  val makeProductsResource: RIO[AppEnvironment, ProductsResource[RIO[AppEnvironment, *]]] = {
    import zio.interop.catz._
    ZIO.runtime[AppEnvironment].map { implicit r: Runtime[AppEnvironment] =>
      new ProductsResource[RIO[AppEnvironment, *]]
    }
  }

  private val productsHandler = new ProductApi()

  def combineRoutes[R](route: HttpRoutes[RIO[R, *]], routes: HttpRoutes[RIO[R, *]]*): HttpRoutes[RIO[R, *]] = {
    import zio.interop.catz._
    import cats.syntax.all._
    routes.fold(route)(_ <+> _)

  }

  val combinedRoutes = {
    import org.http4s.implicits._
    import zio.interop.catz._
    import cats.syntax.all._

    for {
      clientsResource <- makeClientsResource
      clientsRoutes: HttpRoutes[RIO[AppEnvironment, *]] = clientsResource
        .routes(clientsHandler)
      productsResource <- makeProductsResource
      productsRoutes: HttpRoutes[RIO[AppEnvironment, *]] = productsResource.routes(productsHandler)
      combinedRoutes                                     = clientsRoutes <+> productsRoutes

//    } yield clientsResource.routes(clientsHandler).orNotFound
    } yield combinedRoutes.orNotFound
  }

//  val combinedRoutes2 = for {
//    // Get the ClientsResource and ProductsResource instances
//    clientsResource <- makeClientsResource
//    productsResource <- makeProductsResource
//
//    // Transform `clientsRoutes` to accept a combined environment `ClientApi.Environment with ProductApi.Environment`
//    clientsRoutes: HttpRoutes[RIO[ClientApi.Environment with ProductApi.Environment, *]] =
//      clientsResource.routes(clientsHandler).mapK(
//        new (RIO[ClientApi.Environment, *] ~> RIO[ClientApi.Environment with ProductApi.Environment, *]) {
//          def apply[A](fa: RIO[ClientApi.Environment, A]): RIO[ClientApi.Environment with ProductApi.Environment, A] =
//            fa.provideSomeEnvironment[ClientApi.Environment with ProductApi.Environment](_.get[ClientApi.Environment])
//        }
//      )
//
//    // Transform `productsRoutes` to accept a combined environment `ClientApi.Environment with ProductApi.Environment`
//    productsRoutes: HttpRoutes[RIO[ClientApi.Environment with ProductApi.Environment, *]] =
//      productsResource.routes(productsHandler).mapK(
//        new (RIO[ProductApi.Environment, *] ~> RIO[ClientApi.Environment with ProductApi.Environment, *]) {
//          def apply[A](fa: RIO[ProductApi.Environment, A]): RIO[ClientApi.Environment with ProductApi.Environment, A] =
//            fa.provideSomeEnvironment[ClientApi.Environment with ProductApi.Environment](_.get[ProductApi.Environment])
//        }
//      )
//
//    // Now we can combine the two transformed routes into one
//    combinedRoutes: HttpRoutes[RIO[ClientApi.Environment with ProductApi.Environment, *]] =
//      combineRoutes(clientsRoutes, productsRoutes)
//
//  } yield combinedRoutes.orNotFound

  val server: ZIO[AppEnvironment with HttpServer, Throwable, Nothing] =
    for {
      combinedRoutes <- combinedRoutes
      binding        <- HttpServer.bindServer(combinedRoutes)
      _              <- ZIO.log(f"Starting server at ${binding.address}")
      useForever     <- ZIO.never
    } yield useForever

}
