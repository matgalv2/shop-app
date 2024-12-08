package io.github.g4lowy.order.domain.repository

import io.github.g4lowy.order.domain.model.{Order, OrderError, OrderId, OrderStatus}
import zio.macros.accessible
import zio.{IO, UIO}

@accessible
trait OrderRepository {
  def create(order: Order): IO[OrderError.ProductsNotFound, OrderId]
  def getAll(offset: Int, limit: Int): UIO[List[Order]]
  def getById(orderId: OrderId): IO[OrderError.NotFound, Order]
  def updateStatus(orderId: OrderId, orderStatus: OrderStatus): IO[OrderError, Unit]
}
