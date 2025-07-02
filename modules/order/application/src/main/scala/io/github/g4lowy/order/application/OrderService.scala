package io.github.g4lowy.order.application

import io.github.g4lowy.abstracttype.Id.UUIDOps
import io.github.g4lowy.broker.application.MessageProducer
import io.github.g4lowy.customer.application.CustomerService
import io.github.g4lowy.customer.domain.repository.CustomerRepository
import io.github.g4lowy.error.ErrorMessage.ErrorToMessageOps
import io.github.g4lowy.order.application.ApplicationOrderError.{CustomerNotFound, ProductsNotFound}
import io.github.g4lowy.order.application.broker.{OrderRequestMessage, Result}
import io.github.g4lowy.order.application.converter.ordersDto.{OrderDetailDtoOps, OrderDtoOps}
import io.github.g4lowy.order.application.dto.OrderDto
import io.github.g4lowy.order.application.error._
import io.github.g4lowy.order.domain.model.{Order, OrderError, OrderId, OrderStatus}
import io.github.g4lowy.order.domain.repository.OrderRepository
import io.github.g4lowy.product.application.ProductService
import io.github.g4lowy.product.domain.repository.ProductRepository
import io.github.g4lowy.union.types.{Union2, Union3}
import io.github.g4lowy.validation.extras.ZIOValidationOps
import io.github.g4lowy.validation.validators.ValidationFailure
import zio.kafka.producer.Producer
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

  def handleOrderRequest(
    orderDto: OrderDto
  ): ZIO[Producer with MessageProducer[OrderRequestMessage, Producer], Nothing, Unit] =
    ZIO.serviceWithZIO[MessageProducer[OrderRequestMessage, Producer]] {
      _.produce(OrderRequestMessage(orderDto))
    }

  def createOrder(orderDto: OrderDto): URIO[OrderRepository & CustomerRepository & ProductRepository, Result] = {
    val productIds = orderDto.details.map(_.productId.toId)
    for {
      productsAndDetailDTOs <- ProductService
        .getMany(productIds)
        .flatMap { foundProducts =>
          ZIO.foreach(orderDto.details) { detail =>
            foundProducts.find(_.productId.value == detail.productId) match {
              case Some(product) => ZIO.succeed(detail -> product)
              case None          => ZIO.dieMessage("Couldn't find at least one of products")
            }
          }
        }
        .mapError(error => ApplicationOrderError.ProductsNotFound(error.productId, error.productIds))
        .mapError(Union3.Third.apply)
      _ <- CustomerService
        .getCustomerById(orderDto.customerId.toId)
        .mapError(error => Union3.Second(CustomerNotFound(error.customerId)))
      orderId = OrderId.generate
      orderDetails = productsAndDetailDTOs.map { case (detailDto, product) =>
        detailDto.toDomain(orderId, product)
      }.toList
      order <- ZIO.fromNotValidated(orderDto.toDomain(orderId, orderDetails)).mapError(Union3.First.apply)
      id    <- OrderRepository.create(order)
    } yield id
  }.either.map {
    case Left(error) =>
      val description = error match {
        case Union3.First(error: ValidationFailure)                       => error.toMessage
        case Union3.Second(error: ApplicationOrderError.CustomerNotFound) => error.toMessage
        case Union3.Third(error: ProductsNotFound)                        => error.toMessage
      }
      Result.Failure(description)

    case Right(orderId) => Result.Success(orderId.value)
  }

  def updateStatus(
    orderId: OrderId,
    orderStatus: OrderStatus
  ): ZIO[OrderRepository, Union2[OrderError.NotFound, OrderError.InvalidStatus], Unit] =
    OrderRepository.updateStatus(orderId, orderStatus)

}
