package io.github.g4lowy.order.infrastructure.model

import io.getquill.Embedded
import io.github.g4lowy.customer.infrastructure.model.CustomerSQL
import io.github.g4lowy.order.domain.model.{ Order, OrderId, OrderStatus, PaymentType, ShipmentType }
import io.github.g4lowy.product.infrastructure.model.ProductSQL

import java.util.UUID

final case class OrderSQL(
  orderId: UUID,
  customerId: UUID,
  status: OrderStatusSQL,
  paymentType: PaymentTypeSQL,
  paymentAddressId: UUID,
  shipmentType: ShipmentTypeSQL,
  shipmentAddressId: Option[UUID]
) extends Embedded {

  def toUnvalidated(
    customer: CustomerSQL,
    paymentAddress: AddressSQL,
    shipmentAddress: Option[AddressSQL],
    details: List[OrderDetailSQL]
  ): Order.Unvalidated =
    Order.Unvalidated(
      orderId         = OrderId.Unvalidated(orderId.toString),
      customer        = customer.toUnvalidated,
      orderStatus     = status.toDomain,
      paymentType     = paymentType.toDomain,
      paymentAddress  = paymentAddress.toUnvalidated,
      shipmentType    = shipmentType.toDomain,
      shipmentAddress = shipmentAddress.map(_.toUnvalidated),
      details         = details.map(_.toUnvalidated)
    )

  def toDomain(
    customer: CustomerSQL,
    paymentAddress: AddressSQL,
    shipmentAddress: Option[AddressSQL],
    details: List[OrderDetailSQL]
  ): Order =
    toUnvalidated(customer, paymentAddress, shipmentAddress, details).unsafeValidation
}

object OrderSQL {
  def fromDomain(order: Order): OrderSQL =
    OrderSQL(
      orderId           = order.orderId.value,
      customerId        = order.customer.customerId.value,
      status            = OrderStatusSQL.fromDomain(order.orderStatus),
      paymentType       = PaymentTypeSQL.fromDomain(order.paymentType),
      paymentAddressId  = order.paymentAddress.addressId.value,
      shipmentType      = ShipmentTypeSQL.fromDomain(order.shipmentType),
      shipmentAddressId = order.shipmentAddress.map(_.addressId.value)
    )
}
