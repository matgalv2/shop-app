import sbt.addCompilerPlugin

Global / onChangedBuildSource := ReloadOnSourceChanges

version := "0.1"
name := "shop-app"
ThisBuild / scalaVersion := "2.13.16"

val common = Seq(
  scalacOptions ++= (if (scalaVersion.value.startsWith("2.12")) Seq("-Ypartial-unification") else Nil),
  // Use zio-test runner
  testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  // Ensure canceling `run` releases socket, no matter what
  run / fork := true,
  scalacOptions += "-Ymacro-annotations",
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.3" cross CrossVersion.full),
  libraryDependencies ++= List(Dependencies.zio.test, Dependencies.zio.testSbt).map(_ % "test")
)

lazy val root = (project in file("."))
  .aggregate(http, validation, error, customerDomain, customerInfrastructure, productDomain, productInfrastructure)
  .dependsOn(http, validation, error, customerDomain, customerInfrastructure, productDomain, productInfrastructure)
  .settings(libraryDependencies += Dependencies.postgresql.postgresql)

def generateServers(files: String*) =
  files.map(filename => ScalaServer(file(s"api/$filename.yaml"), pkg = "http.generated", framework = "http4s")).toList

lazy val apiSpecFiles = List("customerApi", "productApi", "orderApi")

lazy val http = (project in file("http"))
  .settings(common *)
  .settings(Compile / guardrailTasks ++= generateServers(apiSpecFiles *))
  .enablePlugins(GuardrailPlugin)
  .settings(
    libraryDependencies ++= Seq(
      // Depends on http4s-managed cats and circe
      Dependencies.http4s.core,
      Dependencies.http4s.emberClient,
      Dependencies.http4s.emberServer,
      Dependencies.http4s.circe,
      Dependencies.http4s.dsl,
      // ZIO and the interop library
      Dependencies.zio.zio,
      Dependencies.zio.interopCats,
      Dependencies.zio.logging,
      // ZIO config
      Dependencies.zio.config.core,
      Dependencies.zio.config.typesafeConfig,
      Dependencies.zio.config.magnolia,
      // Comcast
      Dependencies.comcast.core,
      // Circe
      Dependencies.circe.generic,
      // Cats
      Dependencies.cats.core,
      Dependencies.cats.effect,
      // PostgreSQL
      Dependencies.postgresql.postgresql,
      // Flyway
      Dependencies.flyway.core,
      Dependencies.flyway.postgres,
      // Quill
      Dependencies.quill.jdbc,
      // Chimney
      Dependencies.scalaland.chimney,
      // Scalatest
      Dependencies.scalatest.scalatest
    )
  )
  .dependsOn(
    abstractTypes,
    error,
    unionTypes,
    broker,
    customerDomain,
    customerApplication,
    customerInfrastructure,
    productDomain,
    productApplication,
    productInfrastructure,
    orderDomain,
    orderApplication,
    orderInfrastructure
  )

lazy val error = (project in file("/modules/common/error"))
  .settings(name := "error")

lazy val validation = (project in file("/modules/common/validation"))
  .settings(name := "validation")
  .settings(libraryDependencies += Dependencies.zio.zio)
  .settings(libraryDependencies += Dependencies.cats.core)

lazy val testUtils = (project in file("/modules/common/testUtils"))
  .settings(name := "test-utils")
  .settings(common *)
  .settings(libraryDependencies += Dependencies.zio.zio)
  .settings(libraryDependencies += Dependencies.quill.zio)
  .settings(libraryDependencies += Dependencies.flyway.core)
  .settings(libraryDependencies += Dependencies.flyway.postgres)
  .settings(libraryDependencies += Dependencies.zio.config.core)
  .settings(libraryDependencies += Dependencies.zio.config.typesafeConfig)
  .settings(libraryDependencies += Dependencies.zio.config.magnolia)
  .settings(libraryDependencies += Dependencies.postgresql.postgresql)
  .dependsOn(validation)

lazy val unionTypes = (project in file("/modules/common/unionTypes"))
  .settings(name := "union-types")

lazy val abstractTypes = (project in file("/modules/common/abstractTypes"))
  .settings(name := "abstract-types")
  .dependsOn(validation)

lazy val broker = (project in file("/modules/broker"))
  .settings(name := "broker")
  .settings(common *)
  .settings(libraryDependencies += Dependencies.zio.zio)
  .settings(libraryDependencies += Dependencies.zio.macros)
  .settings(libraryDependencies += Dependencies.zio.kafka)
  .dependsOn(validation, abstractTypes)

lazy val customerDomain = (project in file("/modules/customer/domain"))
  .settings(name := "customer-domain")
  .settings(common *)
  .settings(libraryDependencies += Dependencies.zio.zio)
  .settings(libraryDependencies += Dependencies.zio.macros)
  .settings(libraryDependencies += Dependencies.cats.core)
  .dependsOn(validation, abstractTypes)

lazy val customerApplication = (project in file("/modules/customer/application"))
  .settings(name := "customer-application")
  .settings(common *)
  .settings(libraryDependencies += Dependencies.zio.zio)
  .settings(libraryDependencies += Dependencies.zio.macros)
  .settings(libraryDependencies += Dependencies.cats.core)
  .dependsOn(customerDomain, validation, abstractTypes)

lazy val customerInfrastructure = (project in file("/modules/customer/infrastructure"))
  .settings(name := "customer-infrastructure")
  .settings(common *)
  .settings(libraryDependencies += Dependencies.zio.zio)
  .settings(libraryDependencies += Dependencies.quill.zio)
  .settings(libraryDependencies += Dependencies.postgresql.postgresql)
  .dependsOn(customerDomain, error, testUtils)

lazy val productDomain = (project in file("/modules/product/domain"))
  .settings(name := "product-domain")
  .settings(common *)
  .settings(libraryDependencies += Dependencies.zio.zio)
  .settings(libraryDependencies += Dependencies.zio.macros)
  .settings(libraryDependencies += Dependencies.cats.core)
  .dependsOn(validation, abstractTypes)

lazy val productApplication = (project in file("/modules/product/application"))
  .settings(name := "product-application")
  .settings(common *)
  .settings(libraryDependencies += Dependencies.zio.zio)
  .settings(libraryDependencies += Dependencies.zio.macros)
  .settings(libraryDependencies += Dependencies.cats.core)
  .dependsOn(productDomain, validation, abstractTypes)

lazy val productInfrastructure = (project in file("/modules/product/infrastructure"))
  .settings(name := "product-infrastructure")
  .settings(common *)
  .settings(libraryDependencies += Dependencies.zio.zio)
  .settings(libraryDependencies += Dependencies.quill.zio)
  .settings(libraryDependencies += Dependencies.postgresql.postgresql)
  .dependsOn(productDomain, error, testUtils)

lazy val orderDomain = (project in file("/modules/order/domain"))
  .settings(name := "order-domain")
  .settings(common *)
  .settings(libraryDependencies += Dependencies.cats.core)
  .settings(libraryDependencies += Dependencies.zio.zio)
  .settings(libraryDependencies += Dependencies.zio.macros)
  .dependsOn(validation, abstractTypes, unionTypes)

lazy val orderApplication = (project in file("/modules/order/application"))
  .settings(name := "order-application")
  .settings(common *)
  .settings(libraryDependencies += Dependencies.cats.core)
  .settings(libraryDependencies += Dependencies.zio.zio)
  .settings(libraryDependencies += Dependencies.zio.macros)
  .settings(libraryDependencies += Dependencies.scalaland.chimney)
  .dependsOn(orderDomain, validation, error, broker, productApplication, customerApplication, abstractTypes, unionTypes)

lazy val orderInfrastructure = (project in file("/modules/order/infrastructure"))
  .settings(name := "order-infrastructure")
  .settings(common *)
  .settings(libraryDependencies += Dependencies.postgresql.postgresql)
  .settings(libraryDependencies += Dependencies.zio.kafka)
  .settings(libraryDependencies += Dependencies.zio.zio)
  .settings(libraryDependencies += Dependencies.quill.zio)
  .dependsOn(
    orderDomain,
    orderApplication,
    broker,
    error,
    testUtils,
    validation,
    customerInfrastructure,
    productInfrastructure
  )
