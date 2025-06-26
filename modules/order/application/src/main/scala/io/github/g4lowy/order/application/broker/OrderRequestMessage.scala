package io.github.g4lowy.order.application.broker

import io.github.g4lowy.broker.Message
import io.github.g4lowy.order.application.dto.OrderDto

import java.time.LocalDateTime
import java.util.UUID

final case class OrderRequestMessage(
  value: OrderDto,
  requestId: UUID          = UUID.randomUUID(),
  createdAt: LocalDateTime = LocalDateTime.now()
) extends Message[OrderDto]
