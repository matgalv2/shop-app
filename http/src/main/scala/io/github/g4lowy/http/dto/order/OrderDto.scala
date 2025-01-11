package io.github.g4lowy.http.dto

import io.github.g4lowy.http.dto.OrderDto.{ AddressDto, OrderDetailDto, PaymentTypeDto, ShipmentTypeDto }

import java.util.UUID

case class OrderDto(
  customerId: UUID,
  orderDetails: Vector[OrderDetailDto],
  paymentType: PaymentTypeDto,
  paymentAddress: AddressDto,
  shipmentType: ShipmentTypeDto,
  shipmentAddress: Option[AddressDto] = None
)

object OrderDto {
  final case class OrderDetailDto(productId: UUID, quantity: Int)

  sealed trait PaymentTypeDto

  object PaymentTypeDto {
    final case object BankTransfer extends PaymentTypeDto
    final case object Card extends PaymentTypeDto
    final case object OnDelivery extends PaymentTypeDto

  }

  final case class AddressDto(
    country: String,
    city: String,
    street: String,
    zipCode: String,
    building: String,
    apartment: Option[String] = None
  )

  sealed trait ShipmentTypeDto

  object ShipmentTypeDto {
    final case object Courier extends ShipmentTypeDto
    final case object Box extends ShipmentTypeDto
    final case object OnPlace extends ShipmentTypeDto

  }

}
