package io.github.g4lowy.order.domain.model

import io.github.g4lowy.abstractType.Id

sealed trait OrderError

object OrderError {
  final case class NotFound(orderId: OrderId) extends OrderError
  final case class InvalidStatus(orderId: OrderId, orderStatus: OrderStatus) extends OrderError
  final case class ProductsNotFound(productId: Id, productIds: List[Id]) extends OrderError
}
