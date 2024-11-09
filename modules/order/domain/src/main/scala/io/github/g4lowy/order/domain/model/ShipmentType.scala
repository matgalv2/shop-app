package io.github.g4lowy.order.domain.model

sealed abstract class ShipmentType(val value: String)

object ShipmentType {
  final case object Courier extends ShipmentType("COURIER")
  final case object Box extends ShipmentType("BOX")
  final case object OnPlace extends ShipmentType("ON_PLACE")

}
