package io.github.g4lowy.product.domain.model

sealed trait ProductError
object ProductError {
  final case class NotFound(productId: ProductId, productIds: List[ProductId]) extends ProductError
}
