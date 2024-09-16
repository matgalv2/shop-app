package io.github.g4lowy.client.model

sealed trait ClientError

object ClientError{
  final case class NotFound(clientId: ClientId) extends ClientError
}
