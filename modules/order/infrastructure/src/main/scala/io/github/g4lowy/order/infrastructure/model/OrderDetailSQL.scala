package io.github.g4lowy.order.infrastructure.model

import io.github.g4lowy.order.domain.model.{ OrderDetail, OrderId }
import io.github.g4lowy.product.domain.model.ProductId

import java.util.UUID

final case class OrderDetailSQL(orderId: UUID, productId: UUID, quantity: Int, pricePerUnit: BigDecimal) {

  def toUnvalidated: OrderDetail.Unvalidated =
    OrderDetail
      .Unvalidated(
        OrderId.Unvalidated(orderId.toString),
        ProductId.Unvalidated(productId.toString),
        quantity,
        pricePerUnit
      )

  def toDomain: OrderDetail = toUnvalidated.unsafeValidation
}
object OrderDetailSQL {
  def fromDomain(orderDetail: OrderDetail): OrderDetailSQL = OrderDetailSQL(
    orderDetail.orderId.value,
    orderDetail.productId.value,
    orderDetail.quantity,
    orderDetail.pricePerUnit
  )
}
