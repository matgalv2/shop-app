package io.github.g4lowy.order.application.dto

import java.time.LocalDateTime
import java.util.UUID

case class OrderDto(
  customerId: UUID,
  details: Vector[OrderDetailDto],
  paymentType: PaymentTypeDto,
  paymentAddress: AddressDto,
  shipmentType: ShipmentTypeDto,
  shipmentAddress: Option[AddressDto] = None,
  createdAt: LocalDateTime
)
