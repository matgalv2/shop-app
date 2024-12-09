package io.github.g4lowy.customer.domain.model



import io.github.g4lowy.abstracttype.Id

import java.util.UUID

final case class CustomerId(value: UUID) extends Id

object CustomerId {
  def generate: CustomerId = CustomerId(UUID.randomUUID)

  def fromUUID(uuid: UUID): CustomerId = CustomerId(uuid)
}
