package io.github.g4lowy.http.middleware

import cats.data._
import org.http4s.server._
import org.http4s.{Request, Response, Status}
import zio._

object DefectHandlingMiddleware {
  def recoverDefectLogged[R]: HttpMiddleware[RIO[R, *]] =
    service =>
      Kleisli { req: Request[RIO[R, *]] =>
        OptionT {
          service.run(req).value.catchAllDefect { err =>
            ZIO
              .logErrorCause(s"Request failed with a defect", Cause.die(err))
              .as(Some(Response[RIO[R, *]](status = Status.InternalServerError)))
          }
        }
      }
}
