package io.github.g4lowy.order.application

import io.github.g4lowy.abstracttype.Id

sealed trait ApplicationOrderError

object ApplicationOrderError {

  final case class ProductsNotFound(productId: Id, productIds: List[Id]) extends ApplicationOrderError
  final case class CustomerNotFound(customerId: Id) extends ApplicationOrderError
}
