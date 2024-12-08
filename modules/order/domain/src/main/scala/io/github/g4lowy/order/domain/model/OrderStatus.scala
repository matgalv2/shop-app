package io.github.g4lowy.order.domain.model

import io.github.g4lowy.order.domain.model.OrderStatus.values

sealed abstract class OrderStatus(val value: String) {
  def canBeReplacedBy(orderStatus: OrderStatus): Boolean = values.indexOf(this) <= values.indexOf(orderStatus)
}

object OrderStatus {

  final case object Created extends OrderStatus("CREATED")
  final case object Cancelled extends OrderStatus("CANCELLED")
  final case object Paid extends OrderStatus("PAID")
  final case object InProgress extends OrderStatus("IN_PROGRESS")
  final case object Sent extends OrderStatus("SENT")
  final case object Delivered extends OrderStatus("DELIVERED")

  private val values = List(Created, Cancelled, Paid, InProgress, Sent, Delivered)
}
