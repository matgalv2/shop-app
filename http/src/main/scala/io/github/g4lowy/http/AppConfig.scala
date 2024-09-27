package io.github.g4lowy.http

import com.comcast.ip4s.{ Host, Port }
import zio.config._
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfig

final case class AppConfig(http: AppConfig.Http, database: AppConfig.Database)

object AppConfig {

  private val appConfigDescriptor: ConfigDescriptor[AppConfig] =
    (ConfigDescriptor.nested("http")(httpDesc) <*> ConfigDescriptor.nested("database")(databaseDesc)) map {
      case (http, db) =>
        AppConfig(http, db)
    }

  val configLive = TypesafeConfig.fromResourcePath(appConfigDescriptor).orDie

  final case class Http(port: Port, host: Host)

  object Http {
    val hostDescriptor: ConfigDescriptor[Host] =
      ConfigDescriptor.string.transformOrFailLeft(Host.fromString(_).toRight("Invalid data"))(_.toString)

    val portDescriptor: ConfigDescriptor[Port] =
      ConfigDescriptor.int.transformOrFailLeft(Port.fromInt(_).toRight("invalid data"))(_.value)
  }

  val httpDesc: ConfigDescriptor[Http] =
    (ConfigDescriptor.nested("port")(Http.portDescriptor) zip ConfigDescriptor.nested("host")(
      Http.hostDescriptor
    )).map { case (port, host) => Http(port, host) }

  final case class Database(url: String, name: String, schema: String, username: String, password: String)

  val databaseDesc = descriptor[Database]

}
