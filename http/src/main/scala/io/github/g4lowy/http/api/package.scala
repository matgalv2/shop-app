package io.github.g4lowy.http

import http.generated.definitions.ErrorResponse

package object api {


  implicit class ErrorResponseOps(private val companion: ErrorResponse.type) extends AnyVal {
    def single(message: String): ErrorResponse = companion(Vector(message))
  }
}
