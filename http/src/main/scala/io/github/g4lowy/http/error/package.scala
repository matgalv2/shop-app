package io.github.g4lowy.http

import io.github.g4lowy.client.domain.model.ClientError
import io.github.g4lowy.error.ErrorMessage

package object error {

  implicit val clientNotFound: ErrorMessage[ClientError.NotFound] = { case ClientError.NotFound(clientId) =>
    s"Client with id: $clientId was not found"
  }
}
