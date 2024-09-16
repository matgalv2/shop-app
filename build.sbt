Global / onChangedBuildSource := ReloadOnSourceChanges

version := "0.1"
name := "shop-app"
ThisBuild / scalaVersion := "2.13.12"

// Convenience for cross-compat testing
ThisBuild / crossScalaVersions := Seq("2.12.14", "2.13.12")
ThisBuild / scalafixScalaBinaryVersion := CrossVersion.binaryScalaVersion(scalaVersion.value)

val commonSettings = Seq(
  scalacOptions ++= (if (scalaVersion.value.startsWith("2.12")) Seq("-Ypartial-unification") else Nil),
  // Use zio-test runner
  testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
  // Ensure canceling `run` releases socket, no matter what
  run / fork := true
)


//lazy val root = (project in file("."))
//  .settings(
//    name := "shop-app",
//    idePackagePrefix := Some("io.github.g4lowy")
//  )
//  .aggregate()


def generateServers(files: String*) =
  files.map(filename => ScalaServer(file(filename), pkg = "http.generated", framework = "http4s")).toList

lazy val apiSpecFiles = List("userApi", "productApi", "orderApi")


lazy val http = (project in file("http"))
  .settings(commonSettings)
  .settings(
    Compile / guardrailTasks ++= generateServers(apiSpecFiles *)
  )
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
      Dependencies.zio.test,
      Dependencies.zio.testSbt,
      Dependencies.zio.logging,
      // ZIO config
      Dependencies.zio.config.core,
      Dependencies.zio.config.typesafeConfig,
      Dependencies.zio.config.magnolia,
      Dependencies.zio.magic,
      // Comcast
      Dependencies.comcast.core,
      // Cats
      Dependencies.cats.core,
      Dependencies.cats.effect,
      Dependencies.cats.slf4jCats,
//      // PostgreSQL
//      Dependencies.postgresql.postgresql,
      // Quill
      Dependencies.quill.quillJdbc,
      Dependencies.h2database.h2,
      Dependencies.flyway.core

    )
  )
  .settings(dependencyOverrides += Dependencies.comcast.core)
  .dependsOn(clientDomain, clientInfrastructure, logging, error)

lazy val clientDomain = (project in file("/modules/client/domain"))
  .settings(name := "client-domain")
  .settings(libraryDependencies += Dependencies.zio.zio)
  .settings(libraryDependencies += Dependencies.zio.macros)
  .settings(libraryDependencies += Dependencies.cats.core)
  .dependsOn(validation)

lazy val clientInfrastructure = (project in file("/modules/client/infrastructure"))
  .settings(name := "client-infrastructure")
  .settings(libraryDependencies += Dependencies.zio.zio)
  .settings(libraryDependencies += Dependencies.zio.sql)
  .dependsOn(clientDomain)

lazy val error = (project in file("/modules/common/error"))
  .settings(name := "error")

lazy val logging = (project in file("/modules/common/logging"))
  .settings(name := "logging")
  .settings(libraryDependencies += Dependencies.zio.zio)

lazy val validation = (project in file("/modules/common/validation"))
  .settings(name := "validation")
  .settings(libraryDependencies += Dependencies.zio.zio)
  .settings(libraryDependencies += Dependencies.cats.core)


