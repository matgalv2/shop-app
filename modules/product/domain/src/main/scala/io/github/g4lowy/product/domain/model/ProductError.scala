package io.github.g4lowy.product.domain.model

import io.github.g4lowy.abstractType.Id

sealed trait ProductError
object ProductError {
  final case class NotFound(productId: Id, productIds: List[Id]) extends ProductError
}
