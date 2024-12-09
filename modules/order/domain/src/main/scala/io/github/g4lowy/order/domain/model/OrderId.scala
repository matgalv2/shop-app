package io.github.g4lowy.order.domain.model

import io.github.g4lowy.abstracttype.Id

import java.util.UUID

final case class OrderId(value: UUID) extends Id

object OrderId {

  def generate: OrderId = OrderId(UUID.randomUUID)

  def fromUUID(uuid: UUID): OrderId = OrderId(uuid)

}
