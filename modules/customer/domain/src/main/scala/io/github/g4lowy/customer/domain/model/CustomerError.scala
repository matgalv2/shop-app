package io.github.g4lowy.customer.domain.model

import io.github.g4lowy.abstracttype.Id

sealed trait CustomerError

object CustomerError {

  final case class NotFound(customerId: Id) extends CustomerError
}
