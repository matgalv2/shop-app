package io.github.g4lowy.order.domain.model

import io.github.g4lowy.abstracttype.Id
import io.github.g4lowy.order.domain.model.address.Address
import io.github.g4lowy.validation.validators.Validator.FailureDescription
import io.github.g4lowy.validation.validators.{NotValidated, Validation, Validator}

final case class Order private (
  orderId: OrderId,
  customerId: Id,
  orderStatus: OrderStatus,
  details: List[OrderDetail],
  paymentType: PaymentType,
  paymentAddress: Address,
  shipmentType: ShipmentType,
  shipmentAddress: Option[Address]
) {

  def totalCost: BigDecimal = details.map(product => product.quantity * product.pricePerUnit).sum
}

object Order {
  final case class Unvalidated(
    orderId: OrderId,
    customerId: Id,
    details: List[OrderDetail.Unvalidated],
    paymentType: PaymentType,
    paymentAddress: Address.Unvalidated,
    shipmentType: ShipmentType,
    shipmentAddress: Option[Address.Unvalidated],
    orderStatus: OrderStatus
  ) extends NotValidated[Order] {
    override def validate: Validation[FailureDescription, Order] =
      for {
        details         <- Validator.validateIterable[OrderDetail, OrderDetail.Unvalidated](details)
        paymentAddress  <- paymentAddress.validate
        shipmentAddress <- Validator.validOrCheck[Address, Address.Unvalidated](shipmentAddress)
      } yield Order(
        orderId,
        customerId,
        orderStatus,
        details.toList,
        paymentType,
        paymentAddress,
        shipmentType,
        shipmentAddress
      )

    override def unsafeValidation: Order =
      Order(
        orderId,
        customerId,
        orderStatus,
        details.map(_.unsafeValidation),
        paymentType,
        paymentAddress.validateUnsafe,
        shipmentType,
        shipmentAddress.map(_.validateUnsafe)
      )
  }
}
