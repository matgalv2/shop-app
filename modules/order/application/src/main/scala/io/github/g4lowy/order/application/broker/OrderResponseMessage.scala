package io.github.g4lowy.order.application.broker

import io.github.g4lowy.broker.application.Message

import java.time.LocalDateTime
import java.util.UUID

final case class OrderResponseMessage(requestId: UUID, value: Result, createdAt: LocalDateTime = LocalDateTime.now())
    extends Message[Result]
