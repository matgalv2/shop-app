package io.github.g4lowy.order.infrastructure.model

import io.getquill.Embedded
import io.github.g4lowy.abstracttype.Id._
import io.github.g4lowy.order.domain.model.{Order, OrderId}

import java.time.LocalDateTime
import java.util.UUID

final case class OrderSQL(
  orderId: UUID,
  customerId: UUID,
  status: OrderStatusSQL,
  paymentType: PaymentTypeSQL,
  paymentAddressId: UUID,
  shipmentType: ShipmentTypeSQL,
  shipmentAddressId: Option[UUID],
  createdAt: LocalDateTime
) extends Embedded {

  def toDomain(
    paymentAddress: AddressSQL,
    shipmentAddress: Option[AddressSQL],
    details: List[OrderDetailSQL]
  ): Order.Unvalidated =
    Order.Unvalidated(
      orderId         = OrderId.fromUUID(orderId),
      customerId      = customerId.toId,
      orderStatus     = status.toDomain,
      paymentType     = paymentType.toDomain,
      paymentAddress  = paymentAddress.toUnvalidated,
      shipmentType    = shipmentType.toDomain,
      shipmentAddress = shipmentAddress.map(_.toUnvalidated),
      details         = details.map(_.toDomain),
      createdAt       = createdAt
    )
}

object OrderSQL {
  def fromDomain(order: Order): OrderSQL =
    OrderSQL(
      orderId           = order.orderId.value,
      customerId        = order.customerId.value,
      status            = OrderStatusSQL.fromDomain(order.orderStatus),
      paymentType       = PaymentTypeSQL.fromDomain(order.paymentType),
      paymentAddressId  = order.paymentAddress.addressId.value,
      shipmentType      = ShipmentTypeSQL.fromDomain(order.shipmentType),
      shipmentAddressId = order.shipmentAddress.map(_.addressId.value),
      createdAt         = order.createdAt
    )
}
