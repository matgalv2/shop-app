package io.github.g4lowy.http.service

import io.github.g4lowy.customer.domain.model.{ CustomerError, CustomerId }
import io.github.g4lowy.customer.domain.repository.CustomerRepository
import io.github.g4lowy.http.ValidationFailure
import io.github.g4lowy.http.converters.orders.{ OrderDetailDtoOps, OrderDtoOps }
import io.github.g4lowy.http.dto.OrderDto
import io.github.g4lowy.order.domain.model.{ Order, OrderError, OrderId, OrderStatus }
import io.github.g4lowy.order.domain.repository.OrderRepository
import io.github.g4lowy.product.domain.model.{ ProductError, ProductId }
import io.github.g4lowy.product.domain.repository.ProductRepository
import io.github.g4lowy.union.types.Union3
import io.github.g4lowy.validation.extras.ZIOValidationOps
import zio.{ &, URIO, ZIO }

import java.util.UUID

object OrderService {

  private val DEFAULT_OFFSET = 0
  private val DEFAULT_LIMIT  = 26

  def getOrders(offset: Option[Int], limit: Option[Int]): URIO[OrderRepository, List[Order]] =
    OrderRepository.getAll(offset.getOrElse(DEFAULT_OFFSET), limit.getOrElse(DEFAULT_LIMIT))

  def getOrderById(orderId: UUID): ZIO[OrderRepository, OrderError.NotFound, Order] = {
    val domainId = OrderId.fromUUID(orderId)
    OrderRepository.getById(domainId)
  }

  def createOrder(orderDto: OrderDto): ZIO[
    OrderRepository & CustomerRepository & ProductRepository,
    Union3[ValidationFailure, CustomerError.NotFound, ProductError.NotFound],
    OrderId
  ] =
    for {
      productsAndDetailDTOs <- ZIO
        .foreach(orderDto.orderDetails) { detailDto =>
          val id = ProductId.fromUUID(detailDto.productId)
          ProductRepository.getById(id).map(_ -> detailDto)
        }
        .mapError(Union3.Third.apply)

      customer <- CustomerRepository.getById(CustomerId.fromUUID(orderDto.customerId)).mapError(Union3.Second.apply)
      orderId = OrderId.generate
      orderDetails = productsAndDetailDTOs.map { case (product, detailDto) =>
        detailDto.toDomain(orderId, product)
      }.toList
      order <- ZIO.fromNotValidated(orderDto.toDomain(customer, orderDetails)).mapError(Union3.First.apply)
      id    <- OrderRepository.create(order).mapError(Union3.Third.apply)
    } yield id

  def updateStatus(orderId: OrderId, orderStatus: OrderStatus): ZIO[OrderRepository, OrderError, Unit] =
    OrderRepository.updateStatus(orderId, orderStatus)

}
