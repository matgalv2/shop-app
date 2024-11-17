package io.github.g4lowy.http.service

import http.generated.definitions.CreateOrder
import io.github.g4lowy.order.domain.model.{ Order, OrderError, OrderId, OrderStatus }
import io.github.g4lowy.order.domain.repository.OrderRepository
import io.github.g4lowy.product.domain.model.{ Product, ProductError, ProductId }
import io.github.g4lowy.product.domain.repository.ProductRepository
import zio.{ URIO, ZIO }

object OrderService {

  private val DEFAULT_OFFSET = 0
  private val DEFAULT_LIMIT  = 26

  def getOrders(offset: Int, limit: Int): URIO[OrderRepository, List[Order]] =
    OrderRepository.getAll(offset, limit)

  def getOrderById(orderId: OrderId): ZIO[OrderRepository, OrderError.NotFound, Order] =
    OrderRepository.getById(orderId)

  private def getProducts(productIds: List[ProductId]): ZIO[ProductRepository, ProductError.NotFound, List[Product]] =
    ZIO.foreach(productIds)(ProductRepository.getById)

  def createOrder(createOrder: CreateOrder): URIO[OrderRepository, OrderId] = ???

  def updateStatus(orderId: OrderId, orderStatus: OrderStatus): ZIO[OrderRepository, OrderError, Unit] =
    OrderRepository.updateStatus(orderId, orderStatus)

}
