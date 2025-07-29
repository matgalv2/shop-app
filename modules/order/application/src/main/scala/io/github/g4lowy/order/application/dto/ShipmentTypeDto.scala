package io.github.g4lowy.order.application.dto

sealed trait ShipmentTypeDto

object ShipmentTypeDto {

  final case object Courier extends ShipmentTypeDto
  final case object Box extends ShipmentTypeDto
  final case object OnPlace extends ShipmentTypeDto
}
