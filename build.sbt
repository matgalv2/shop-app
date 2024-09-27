import sbt.addCompilerPlugin

Global / onChangedBuildSource := ReloadOnSourceChanges

version := "0.1"
name := "shop-app"
ThisBuild / scalaVersion := "2.13.14"

val common = Seq(
  scalacOptions ++= (if (scalaVersion.value.startsWith("2.12")) Seq("-Ypartial-unification") else Nil),
  // Use zio-test runner
  testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  // Ensure canceling `run` releases socket, no matter what
  run / fork := true,
  scalacOptions += "-Ymacro-annotations",
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.3" cross CrossVersion.full)
)

lazy val root = (project in file("."))
  .aggregate(http, validation, error, clientDomain, clientInfrastructure)
  .dependsOn(http, validation, error, clientDomain, clientInfrastructure)

def generateServers(files: String*) =
  files.map(filename => ScalaServer(file(s"api/$filename.yaml"), pkg = "http.generated", framework = "http4s")).toList

lazy val apiSpecFiles = List("clientApi")

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
      Dependencies.zio.test,
      Dependencies.zio.testSbt,
      Dependencies.zio.logging,
      // ZIO config
      Dependencies.zio.config.core,
      Dependencies.zio.config.typesafeConfig,
      Dependencies.zio.config.magnolia,
//      Dependencies.zio.magic,
      // Comcast
      Dependencies.comcast.core,
      // Cats
      Dependencies.cats.core,
      Dependencies.cats.effect,
      // PostgreSQL
      Dependencies.postgresql.postgresql,
      // Flyway
      Dependencies.flyway.core,
      Dependencies.flyway.postgres,
      // Quill
      Dependencies.quill.jdbc
    )
  )
  .dependsOn(clientDomain, clientInfrastructure, error)

lazy val error = (project in file("/modules/common/error"))
  .settings(name := "error")

lazy val validation = (project in file("/modules/common/validation"))
  .settings(name := "validation")
  .settings(libraryDependencies += Dependencies.zio.zio)
  .settings(libraryDependencies += Dependencies.cats.core)

lazy val clientDomain = (project in file("/modules/client/domain"))
  .settings(name := "client-domain")
  .settings(common *)
  .settings(libraryDependencies += Dependencies.zio.zio)
  .settings(libraryDependencies += Dependencies.zio.macros)
  .settings(libraryDependencies += Dependencies.cats.core)
  .dependsOn(validation)

lazy val clientInfrastructure = (project in file("/modules/client/infrastructure"))
  .settings(name := "client-infrastructure")
  .settings(common *)
  .settings(libraryDependencies += Dependencies.zio.zio)
  .settings(libraryDependencies += Dependencies.quill.zio)
  .dependsOn(clientDomain, error)
