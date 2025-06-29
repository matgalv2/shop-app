package io.github.g4lowy.order.application

import java.util.UUID

sealed trait Result

object Result {

  final case class Success(orderId: UUID) extends Result
  final case class Failure(description: String) extends Result
}
