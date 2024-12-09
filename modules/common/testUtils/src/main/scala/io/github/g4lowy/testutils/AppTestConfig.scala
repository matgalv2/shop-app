package io.github.g4lowy.testutils

import zio.ULayer
import zio.config._
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfig

final case class AppTestConfig(database: AppTestConfig.Database)

object AppTestConfig {

  private val integrationTestApplicationConfigFilePath =
    "modules/common/testUtils/src/main/resources/application-integration_test.conf"
  private val acceptanceTestApplicationConfigFilePath =
    "modules/common/testUtils/src/main/resources/application-acceptance_test.conf"

  private val testConfigDescriptor: ConfigDescriptor[AppTestConfig] =
    ConfigDescriptor.nested("database")(databaseDesc).to[AppTestConfig]

  private def testConfigLive(hoconFilePath: String): ULayer[AppTestConfig] = TypesafeConfig
    .fromHoconFilePath(filePath = hoconFilePath, testConfigDescriptor)
    .orDie

  def integrationTestConfigLive: ULayer[AppTestConfig] = testConfigLive(integrationTestApplicationConfigFilePath)

  def acceptanceTestConfigLive: ULayer[AppTestConfig] = testConfigLive(acceptanceTestApplicationConfigFilePath)

  final case class Database(url: String, name: String, schema: String, username: String, password: String)

  private val databaseDesc = descriptor[Database]
}
