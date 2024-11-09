package io.github.g4lowy.order.domain.repository

import io.github.g4lowy.order.domain.model.{ Order, OrderError, OrderId, OrderStatus }
import zio.{ IO, UIO }
import zio.macros.accessible

@accessible
trait OrderRepository {
  def create(order: Order): UIO[OrderId]
  def getAll: UIO[List[Order]]
  def getById(orderId: OrderId): IO[OrderError.NotFound, Order]
  def updateStatus(orderId: OrderId, orderStatus: OrderStatus): IO[OrderError, Unit]
}
