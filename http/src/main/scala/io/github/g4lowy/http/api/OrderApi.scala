package io.github.g4lowy.http.api

import http.generated.definitions.{CreateOrder, ErrorResponse, Message, PatchOrder}
import http.generated.orders
import http.generated.orders._
import io.github.g4lowy.broker.application.MessageProducer
import io.github.g4lowy.customer.domain.repository.CustomerRepository
import io.github.g4lowy.error.ErrorMessage._
import io.github.g4lowy.http.AppEnvironment
import io.github.g4lowy.http.api.OrderApi.Environment
import io.github.g4lowy.http.converters.orders.{CreateOrderDtoOps, OrderOps, PatchOrderStatusOps}
import io.github.g4lowy.http.error._
import io.github.g4lowy.order.application.OrderService
import io.github.g4lowy.order.application.broker.OrderRequestMessage
import io.github.g4lowy.order.domain.model.{OrderError, OrderId}
import io.github.g4lowy.order.domain.repository.OrderRepository
import io.github.g4lowy.product.domain.repository.ProductRepository
import io.github.g4lowy.union.types.Union2
import org.http4s.HttpRoutes
import zio.kafka.producer.Producer
import zio.{&, RIO, Runtime, URIO, ZIO}

import java.util.UUID

class OrderApi extends OrdersHandler[RIO[AppEnvironment, *]] {
  override def createOrder(
    respond: CreateOrderResponse.type
  )(body: CreateOrder): RIO[Environment, CreateOrderResponse] =
    OrderService
      .handleOrder(body.toDTO)
      .as(respond.Created(Message("Request has been created and is being processed now.")))

  override def getAllOrders(
    respond: GetAllOrdersResponse.type
  )(offset: Option[Int], limit: Option[Int]): RIO[Environment, GetAllOrdersResponse] =
    OrderService
      .getOrders(offset, limit)
      .map(_.map(_.toAPI).toVector)
      .map(respond.Ok)

  override def getOrderById(respond: GetOrderByIdResponse.type)(orderId: UUID): RIO[Environment, GetOrderByIdResponse] =
    OrderService
      .getOrderById(orderId)
      .map(_.toAPI)
      .mapBoth(error => respond.NotFound(ErrorResponse.single(error.toMessage)), respond.Ok)
      .merge

  override def updateStatus(
    respond: orders.UpdateStatusResponse.type
  )(orderId: UUID, body: PatchOrder): RIO[Environment, UpdateStatusResponse] =
    OrderService
      .updateStatus(OrderId.fromUUID(orderId), body.status.toDTO)
      .mapBoth(
        {
          case _ @Union2.First(err: OrderError.NotFound) => respond.NotFound(ErrorResponse.single(err.toMessage))
          case _ @Union2.Second(err: OrderError.InvalidStatus) =>
            respond.BadRequest(ErrorResponse.single(err.toMessage))
        },
        _ => respond.NoContent
      )
      .merge
}

object OrderApi {
  type Environment = OrderRepository & ProductRepository & CustomerRepository & Producer & MessageProducer[OrderRequestMessage, Producer]

  val routes: URIO[AppEnvironment, HttpRoutes[RIO[AppEnvironment, *]]] = {
    import zio.interop.catz._

    ZIO
      .runtime[AppEnvironment]
      .map { implicit r: Runtime[AppEnvironment] =>
        new OrdersResource[RIO[AppEnvironment, *]]
      }
      .map(_.routes(new OrderApi))
  }
}
