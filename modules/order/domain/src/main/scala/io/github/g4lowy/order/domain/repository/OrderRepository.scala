package io.github.g4lowy.order.domain.repository

import io.github.g4lowy.order.domain.model.{Order, OrderError, OrderId, OrderStatus}
import io.github.g4lowy.product.domain.model.ProductError
import zio.{IO, UIO}
import zio.macros.accessible

@accessible
trait OrderRepository {
  def create(order: Order): IO[ProductError.NotFound, OrderId]
  def getAll(offset: Int, limit: Int): UIO[List[Order]]
  def getById(orderId: OrderId): IO[OrderError.NotFound, Order]
  def updateStatus(orderId: OrderId, orderStatus: OrderStatus): IO[OrderError, Unit]
}
