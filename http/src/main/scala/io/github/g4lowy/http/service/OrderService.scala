package io.github.g4lowy.http.service

import io.github.g4lowy.abstracttype.Id.UUIDOps
import io.github.g4lowy.customer.domain.model.{CustomerError, CustomerId}
import io.github.g4lowy.customer.domain.repository.CustomerRepository
import io.github.g4lowy.http.ValidationFailure
import io.github.g4lowy.http.converters.orders.{OrderDetailDtoOps, OrderDtoOps}
import io.github.g4lowy.http.dto.OrderDto
import io.github.g4lowy.order.domain.model.{Order, OrderError, OrderId, OrderStatus}
import io.github.g4lowy.order.domain.repository.OrderRepository
import io.github.g4lowy.product.domain.model.{ProductError, ProductId}
import io.github.g4lowy.product.domain.repository.ProductRepository
import io.github.g4lowy.union.types.{Union2, Union3}
import io.github.g4lowy.validation.extras.ZIOValidationOps
import zio.{&, URIO, ZIO}

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
    Union3[ValidationFailure, CustomerError.NotFound, OrderError.ProductsNotFound],
    OrderId
  ] = {
    val productIds = orderDto.orderDetails.map(_.productId.toId)
    for {
      productsAndDetailDTOs <- ProductService
        .getMany(productIds.map(id => ProductId.apply(id.value)).toList)
        .flatMap { foundProducts =>
          ZIO.foreach(orderDto.orderDetails) { detail =>
            foundProducts.find(_.productId.value == detail.productId) match {
              case Some(product) => ZIO.succeed(detail -> product)
              case None          => ZIO.dieMessage("There was an error grouping products by id")
            }
          }
        }
        .mapError { case ProductError.NotFound(id, ids) =>
          OrderError.ProductsNotFound(id, ids)
        }
        .mapError(Union3.Third.apply)
      _ <- CustomerRepository.getById(CustomerId.fromUUID(orderDto.customerId)).mapError(Union3.Second.apply)
      orderId = OrderId.generate
      orderDetails = productsAndDetailDTOs.map { case (detailDto, product) =>
        detailDto.toDomain(orderId, product)
      }.toList
      order <- ZIO.fromNotValidated(orderDto.toDomain(orderId, orderDetails)).mapError(Union3.First.apply)
      id    <- OrderRepository.create(order).mapError(Union3.Third.apply)
    } yield id
  }

  def updateStatus(
    orderId: OrderId,
    orderStatus: OrderStatus
  ): ZIO[OrderRepository, Union2[OrderError.NotFound, OrderError.InvalidStatus], Unit] =
    OrderRepository.updateStatus(orderId, orderStatus)

}
