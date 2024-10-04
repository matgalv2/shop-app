package io.github.g4lowy.test.utils

import zio.ULayer
import zio.config._
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfig

final case class AppTestConfig(database: AppTestConfig.Database)

object AppTestConfig {

  private val testConfigDescriptor: ConfigDescriptor[AppTestConfig] =
    ConfigDescriptor.nested("database")(databaseDesc).to[AppTestConfig]

  def testConfigLive(): ULayer[AppTestConfig] = TypesafeConfig
    .fromHoconFilePath(
      filePath = "modules/common/testUtils/src/main/resources/application-test.conf",
      testConfigDescriptor
    )
    .orDie

  final case class Database(url: String, name: String, schema: String, username: String, password: String)

  private val databaseDesc = descriptor[Database]
}
