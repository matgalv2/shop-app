package io.github.g4lowy.order.domain.model

import io.github.g4lowy.customer.domain.model.{ Customer, CustomerId }
import io.github.g4lowy.order.domain.model
import io.github.g4lowy.validation.validators.Validator.FailureDescription
import io.github.g4lowy.validation.validators.{ NotValidated, Validation, Validator }

final case class Order private (
  orderId: OrderId,
  customer: Customer,
  status: OrderStatus,
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
    orderId: OrderId.Unvalidated,
    customer: Customer.Unvalidated,
    details: List[OrderDetail.Unvalidated],
    paymentType: PaymentType,
    paymentAddress: Address.Unvalidated,
    shipmentType: ShipmentType,
    shipmentAddress: Option[Address.Unvalidated],
    orderStatus: OrderStatus
  ) extends NotValidated[Order] {
    override def validate: Validation[FailureDescription, Order] =
      for {
        orderId         <- orderId.validate
        customer        <- customer.validate
        details         <- Validator.validateIterable[OrderDetail, model.OrderDetail.Unvalidated](details)
        paymentAddress  <- paymentAddress.validate
        shipmentAddress <- Validator.validOrCheck[Address, Address.Unvalidated](shipmentAddress)
      } yield Order(
        orderId,
        customer,
        orderStatus,
        details.toList,
        paymentType,
        paymentAddress,
        shipmentType,
        shipmentAddress
      )

    override def unsafeValidation: Order =
      Order(
        orderId.unsafeValidation,
        customer.unsafeValidation,
        orderStatus,
        details.map(_.unsafeValidation),
        paymentType,
        paymentAddress.unsafeValidation,
        shipmentType,
        shipmentAddress.map(_.unsafeValidation)
      )
  }
}
