package io.github.g4lowy.order.domain.model

import io.github.g4lowy.abstracttype.Id
import io.github.g4lowy.validation.validators.Validator._
import io.github.g4lowy.validation.validators.{NotValidated, Validation}

final case class OrderDetail private (orderId: OrderId, productId: Id, quantity: Int, pricePerUnit: BigDecimal)

object OrderDetail {
  final case class Unvalidated(orderId: OrderId, productId: Id, quantity: Int, pricePerUnit: BigDecimal)
      extends NotValidated[OrderDetail] {
    override def validate: Validation[FailureDescription, OrderDetail] =
      for {

        quantity     <- min(1).apply(quantity)
        pricePerUnit <- min(BigDecimal(0.01)).apply(pricePerUnit)
      } yield OrderDetail(orderId, productId, quantity, pricePerUnit)

    override def unsafeValidation: OrderDetail =
      OrderDetail(orderId = orderId, productId = productId, quantity = quantity, pricePerUnit = pricePerUnit)
  }
}
