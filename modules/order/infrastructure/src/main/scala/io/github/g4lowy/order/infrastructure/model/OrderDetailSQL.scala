package io.github.g4lowy.order.infrastructure.model

import io.github.g4lowy.abstractType.Id._
import io.github.g4lowy.order.domain.model.{OrderDetail, OrderId}

import java.util.UUID

final case class OrderDetailSQL(orderId: UUID, productId: UUID, quantity: Int, pricePerUnit: BigDecimal) {

  def toDomain: OrderDetail.Unvalidated =
    OrderDetail
      .Unvalidated(OrderId.fromUUID(orderId), productId.toId, quantity, pricePerUnit)
}

object OrderDetailSQL {
  def fromDomain(orderDetail: OrderDetail): OrderDetailSQL = OrderDetailSQL(
    orderDetail.orderId.value,
    orderDetail.productId.value,
    orderDetail.quantity,
    orderDetail.pricePerUnit
  )
}
