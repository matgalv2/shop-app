package io.github.g4lowy.http

import com.comcast.ip4s.{ Host, Port }
import zio.ULayer
import zio.config._
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfig

final case class AppConfig(http: AppConfig.Http, database: AppConfig.Database)

object AppConfig {

  private val appConfigDescriptor: ConfigDescriptor[AppConfig] =
    (ConfigDescriptor.nested("http")(httpDesc) <*> ConfigDescriptor.nested("database")(databaseDesc)).to[AppConfig]

  val live: ULayer[AppConfig] = TypesafeConfig.fromResourcePath(appConfigDescriptor).orDie

  final case class Http(port: Port, host: Host)

  object Http {
    val hostDescriptor: ConfigDescriptor[Host] =
      ConfigDescriptor.string
        .transformOrFailLeft(Host.fromString(_).toRight("Could not derive host configuration"))(_.toString)

    val portDescriptor: ConfigDescriptor[Port] =
      ConfigDescriptor.int.transformOrFailLeft(Port.fromInt(_).toRight("Could not derive port configuration"))(_.value)
  }

  private val httpDesc: ConfigDescriptor[Http] =
    (ConfigDescriptor.nested("port")(Http.portDescriptor) <*> ConfigDescriptor.nested("host")(Http.hostDescriptor))
      .to[Http]

  final case class Database(url: String, name: String, schema: String, username: String, password: String)

  private val databaseDesc = descriptor[Database]
}
