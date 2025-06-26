package io.github.g4lowy.order.application.dto

import java.util.UUID

final case class OrderDetailDto(productId: UUID, quantity: Int)
