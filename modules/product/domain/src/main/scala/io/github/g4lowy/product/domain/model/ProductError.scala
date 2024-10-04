package io.github.g4lowy.product.domain.model

trait ProductError
object ProductError {
  final case class NotFound(productId: ProductId) extends ProductError
}
