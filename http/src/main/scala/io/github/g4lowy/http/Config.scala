package io.github.g4lowy.http

import com.comcast.ip4s.{Host, Port}
import zio.config._
import zio.config.magnolia._

final case class Config(http: Config.Http, database: Config.Database)

object Config {
  implicit val descriptor: ConfigDescriptor[Config] =
    (ConfigDescriptor.nested("http")(Http.httpDescriptor)
      |@| ConfigDescriptor.nested("database")(databaseDescriptor))(Config.apply, Config.unapply)

  final case class Http(port: Port, host: Host)

  object Http {
    implicit val hostDescriptor: ConfigDescriptor[Host] =
      ConfigDescriptor.string.transformOrFailLeft(Host.fromString(_).toRight("No host found"))(_.toString())

    implicit val portDescriptor: ConfigDescriptor[Port] =
      ConfigDescriptor.int.transformOrFailLeft(Port.fromInt(_).toRight("No port found"))(_.value)

    implicit val httpDescriptor: ConfigDescriptor[Http] = (ConfigDescriptor.nested("port")(
      portDescriptor
    ) |@| ConfigDescriptor.nested("host")(hostDescriptor))(Http.apply, Http.unapply)
  }

  final case class Database(url: String, database: String, schema: String, user: String, password: String)

  implicit val databaseDescriptor: ConfigDescriptor[Database] = DeriveConfigDescriptor.descriptor

}
