package io.github.g4lowy.customer.domain.model

sealed trait CustomerError

object CustomerError{
  final case class NotFound(clientId: CustomerId) extends CustomerError
}
