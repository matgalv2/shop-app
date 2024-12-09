package io.github.g4lowy.order.domain.model.address

import io.github.g4lowy.abstracttype.Id

import java.util.UUID

final case class AddressId(value: UUID) extends Id

object AddressId {

  def generate: AddressId = AddressId.apply(UUID.randomUUID)

  def fromUUID(uuid: UUID): AddressId = AddressId(uuid)
}
