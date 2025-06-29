package io.github.g4lowy.order.application.broker

import io.github.g4lowy.broker.application.Message
import io.github.g4lowy.order.application.Result

import java.time.LocalDateTime
import java.util.UUID

final case class OrderResponseMessage(
  value: Result,
  requestId: UUID          = UUID.randomUUID(),
  createdAt: LocalDateTime = LocalDateTime.now()
) extends Message[Result]
