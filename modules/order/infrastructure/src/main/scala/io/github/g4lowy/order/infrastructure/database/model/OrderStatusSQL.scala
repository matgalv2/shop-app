package io.github.g4lowy.order.infrastructure.database.model

import io.github.g4lowy.order.domain.model.OrderStatus
import io.github.g4lowy.testutils.enums.{EnumDecoder, EnumSQL}

sealed abstract class OrderStatusSQL(val value: String) extends EnumSQL {

  def toDomain: OrderStatus = this match {
    case OrderStatusSQL.Created    => OrderStatus.Created
    case OrderStatusSQL.Cancelled  => OrderStatus.Cancelled
    case OrderStatusSQL.Paid       => OrderStatus.Paid
    case OrderStatusSQL.InProgress => OrderStatus.InProgress
    case OrderStatusSQL.Sent       => OrderStatus.Sent
    case OrderStatusSQL.Delivered  => OrderStatus.Delivered
    case OrderStatusSQL.Archived   => OrderStatus.Delivered
  }
}

object OrderStatusSQL extends EnumDecoder[OrderStatusSQL] {

  final case object Created extends OrderStatusSQL("CREATED")
  final case object Cancelled extends OrderStatusSQL("CANCELLED")
  final case object Paid extends OrderStatusSQL("PAID")
  final case object InProgress extends OrderStatusSQL("IN_PROGRESS")
  final case object Sent extends OrderStatusSQL("SENT")
  final case object Delivered extends OrderStatusSQL("DELIVERED")
  final case object Archived extends OrderStatusSQL("ARCHIVED")

  override protected val values: List[OrderStatusSQL] =
    List(Created, Cancelled, Paid, InProgress, Sent, Delivered, Archived)

  def fromDomain(status: OrderStatus): OrderStatusSQL = status match {
    case OrderStatus.Created    => OrderStatusSQL.Created
    case OrderStatus.Cancelled  => OrderStatusSQL.Cancelled
    case OrderStatus.Paid       => OrderStatusSQL.Paid
    case OrderStatus.InProgress => OrderStatusSQL.InProgress
    case OrderStatus.Sent       => OrderStatusSQL.Sent
    case OrderStatus.Delivered  => OrderStatusSQL.Delivered
    case OrderStatus.Archived   => OrderStatusSQL.Archived
  }
}
