package io.github.g4lowy.order.domain.model

import io.github.g4lowy.product.domain.model.ProductId
import io.github.g4lowy.validation.validators.Validator.FailureDescription
import io.github.g4lowy.validation.validators.{ NotValidated, Validation }
import io.github.g4lowy.validation.validators.Validator._

final case class OrderDetail private (orderId: OrderId, productId: ProductId, quantity: Int, pricePerUnit: BigDecimal)

object OrderDetail {
  final case class Unvalidated(
    orderId: OrderId.Unvalidated,
    productId: ProductId.Unvalidated,
    quantity: Int,
    pricePerUnit: BigDecimal
  ) extends NotValidated[OrderDetail] {
    override def validate: Validation[FailureDescription, OrderDetail] =
      for {
        orderId      <- orderId.validate
        productId    <- productId.validate
        quantity     <- min(1).apply(quantity)
        pricePerUnit <- min(BigDecimal(0.01)).apply(pricePerUnit)
      } yield OrderDetail(orderId, productId, quantity, pricePerUnit)

    override def unsafeValidation: OrderDetail =
      OrderDetail(
        orderId      = orderId.unsafeValidation,
        productId    = productId.unsafeValidation,
        quantity     = quantity,
        pricePerUnit = pricePerUnit
      )
  }
}
