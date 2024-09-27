package io.github.g4lowy.http

import com.comcast.ip4s.{ Host, Port }
import org.http4s.server.Server
import zio.{ RIO, URLayer, ZIO, ZLayer }

case class HttpServer(port: Port, host: Host) {

  def bindServer[R](httpApp: org.http4s.Http[RIO[R, *], RIO[R, *]]): ZIO[R, Throwable, Server[RIO[R, *]]] = {
    import zio.interop.catz._
    import zio.interop.catz.implicits._

    import cats.effect._
    import org.http4s.ember.server.EmberServerBuilder

    implicit val timer: Timer[RIO[R, *]] = ioTimer[R, Throwable]

    ZIO.runtime[R].flatMap { implicit r: zio.Runtime[R] =>
      val resource: Resource[RIO[R, *], Server[RIO[R, *]]] = EmberServerBuilder
        .default[RIO[R, *]]
        .withHttpApp(httpApp)
        .withHost(host.toString)
        .withPort(port.value)
        .build

      resource.allocated.map(_._1)
    }
  }
}

object HttpServer {

  val live: URLayer[AppConfig, HttpServer] = ZLayer {
    for {
      config <- ZIO.service[AppConfig]
    } yield HttpServer(config.http.port, config.http.host)
  }

  def bindServer[R](
    httpApp: org.http4s.Http[RIO[R, *], RIO[R, *]]
  ): ZIO[R with HttpServer, Throwable, Server[RIO[R, *]]] =
    ZIO.serviceWithZIO[HttpServer](_.bindServer(httpApp))

  def host: RIO[HttpServer, String] =
    ZIO.serviceWith[HttpServer](_.host.toString)

  def port: RIO[HttpServer, String] =
    ZIO.serviceWith[HttpServer](_.port.toString)

}
