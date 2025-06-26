package io.github.g4lowy.order.application.dto

import io.github.g4lowy.order.domain.model.OrderStatus

sealed abstract class OrderStatusDto(val value: String)

object OrderStatusDto {

  final case object Created extends OrderStatusDto(OrderStatus.Created.toString)
  final case object Cancelled extends OrderStatusDto(OrderStatus.Cancelled.toString)
  final case object Paid extends OrderStatusDto(OrderStatus.Paid.toString)
  final case object InProgress extends OrderStatusDto(OrderStatus.InProgress.toString)
  final case object Sent extends OrderStatusDto(OrderStatus.Sent.toString)
  final case object Delivered extends OrderStatusDto(OrderStatus.Delivered.toString)
  final case object Archived extends OrderStatusDto(OrderStatus.Archived.toString)
}
