package io.github.g4lowy.product.domain.model

import io.github.g4lowy.abstractType.Id

import java.util.UUID

final case class ProductId(value: UUID) extends Id

object ProductId {

  def generate: ProductId = ProductId(UUID.randomUUID)

  def fromUUID(uuid: UUID): ProductId = ProductId(uuid)

}
