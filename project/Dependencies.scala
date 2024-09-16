import sbt.*

object Dependencies {
  object zio {
    lazy val zio         = "dev.zio"              %% "zio"              % V.zio
    lazy val interopCats = "dev.zio"              %% "zio-interop-cats" % V.interopCats
    lazy val test        = "dev.zio"              %% "zio-test"         % V.zio
    lazy val testSbt     = "dev.zio"              %% "zio-test-sbt"     % V.zio
    lazy val macros      = "dev.zio"              %% "zio-macros"       % V.zio
    lazy val magic       = "io.github.kitlangton" %% "zio-magic"        % V.zioMagic
    lazy val logging     = "dev.zio"              %% "zio-logging"      % V.zioLogging
    lazy val sql         = "dev.zio"              %% "zio-sql-postgres" % V.zioSql

    object config {
      lazy val core           = "dev.zio" %% "zio-config"          % V.zioConfig
      lazy val typesafeConfig = "dev.zio" %% "zio-config-typesafe" % V.zioConfig
      lazy val magnolia       = "dev.zio" %% "zio-config-magnolia" % V.zioConfig
    }

  }
  object http4s {
    lazy val core        = "org.http4s" %% "http4s-core"         % V.http4s
    lazy val circe       = "org.http4s" %% "http4s-circe"        % V.http4s
    lazy val emberServer = "org.http4s" %% "http4s-ember-server" % V.http4s
    lazy val dsl         = "org.http4s" %% "http4s-dsl"          % V.http4s
    lazy val emberClient = "org.http4s" %% "http4s-ember-client" % V.http4s
  }
  object cats {
    lazy val core      = "org.typelevel" %% "cats-core"      % V.cats
    lazy val effect    = "org.typelevel" %% "cats-effect"    % V.catsEffect
    lazy val slf4jCats = "org.typelevel" %% "log4cats-slf4j" % V.slf4jCats
  }

  object circe {
    lazy val generic = "io.circe" %% "circe-generic" % V.circe
    lazy val core    = "io.circe" %% "circe-core"    % V.circe
  }


  object comcast {
    lazy val core = "com.comcast" %% "ip4s-core" % V.comcast
  }

//  object postgresql {
//    lazy val postgresql = "org.postgresql" % "postgresql" % V.postgresql
//  }

  object h2database {
    lazy val h2 = "com.h2database" % "h2" % V.h2
  }

  object flyway {
    lazy val core = "org.flywaydb" % "flyway-core" % V.flyway

  }

  object quill {
    lazy val quillJdbc = "io.getquill" %% "quill-jdbc" % V.quill
  }

  object typesafe {
    lazy val config = "com.typesafe" % "config" % V.typesafe
  }

  object V {
    val zio         = "1.0.18" //"2.0.9"
    val zioConfig   = "1.0.10"
    val zioSql      = "0.1.2"
    val http4s      = "0.21.24"
    val cats        = "2.8.0"
    val catsEffect  = "2.1.0"
    val circe       = "0.13.0"
    val interopCats = "2.5.1.0"
    val zioMagic    = "0.3.12"
    val comcast     = "3.1.3"
    val slf4jCats   = "1.3.1"
    val zioLogging  = "0.5.13"
    val typesafe    = "1.4.2"
    val postgresql  = "42.7.4"
    val quill       = "4.7.0"
    val h2          = "2.2.224"
    val flyway      = "10.18.0"

  }
}
