package io.github.g4lowy.order.domain.model


sealed trait OrderError

object OrderError {
  final case class NotFound(orderId: OrderId) extends OrderError
  final case class InvalidStatus(orderId: OrderId, orderStatus: OrderStatus) extends OrderError
}
