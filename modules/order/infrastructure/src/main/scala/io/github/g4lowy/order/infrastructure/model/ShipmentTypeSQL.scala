package io.github.g4lowy.order.infrastructure.model

import io.github.g4lowy.order.domain.model.ShipmentType
import io.github.g4lowy.test.utils.enums.{ EnumDecoder, EnumSQL }

sealed abstract class ShipmentTypeSQL(val value: String) extends EnumSQL {
  def toDomain: ShipmentType = this match {
    case ShipmentTypeSQL.Courier => ShipmentType.Courier
    case ShipmentTypeSQL.Box     => ShipmentType.Box
    case ShipmentTypeSQL.OnPlace => ShipmentType.OnPlace
  }
}

object ShipmentTypeSQL extends EnumDecoder[ShipmentTypeSQL] {
  final case object Courier extends ShipmentTypeSQL("COURIER")
  final case object Box extends ShipmentTypeSQL("BOX")
  final case object OnPlace extends ShipmentTypeSQL("ON_PLACE")

  override protected val values: List[ShipmentTypeSQL] = List(Courier, Box, OnPlace)

  def fromDomain(shipmentType: ShipmentType): ShipmentTypeSQL = shipmentType match {
    case ShipmentType.Courier => ShipmentTypeSQL.Courier
    case ShipmentType.Box     => ShipmentTypeSQL.Box
    case ShipmentType.OnPlace => ShipmentTypeSQL.OnPlace
  }

}
