package io.github.g4lowy.http

import http.generated.clients.ClientsResource
import io.github.g4lowy.http.api.ClientApi
import zio.{ &, RIO, Runtime, ZIO }

object Controller {

  /** An effect which, when executed, gives a MlResource (capable of transforming a MlHandler into something
    * bindable)
    */
  val makeClientsResource: RIO[ClientApi.Environment, ClientsResource[RIO[ClientApi.Environment, *]]] = {
    import zio.interop.catz._
    ZIO.runtime[ClientApi.Environment].map { implicit r: Runtime[ClientApi.Environment] =>
      new ClientsResource[RIO[ClientApi.Environment, *]]
    }
  }

  /** HTTP server implementation, utilizing the SalaryPredictor Layer
    */
  private val clientsHandler = new ClientApi()

  val combinedRoutes = {
    import org.http4s.implicits._
    import zio.interop.catz._

    for {
      mlResource <- makeClientsResource
    } yield mlResource.routes(clientsHandler).orNotFound
  }

  val server =
    for {
      combinedRoutes <- combinedRoutes
      binding        <- HttpServer.bindServer(combinedRoutes)
      _              <- ZIO.log(f"Starting server at ${binding.address}")
      useForever     <- ZIO.never
    } yield useForever

}
