package io.github.g4lowy.http.api

import http.generated.definitions.{ CreateOrder, PatchOrder }
import http.generated.orders.{
  CreateOrderResponse,
  GetAllOrdersResponse,
  GetOrderByIdResponse,
  OrdersHandler,
  OrdersResource,
  PatchOrderResponse
}
import io.github.g4lowy.http.AppEnvironment
import io.github.g4lowy.http.api.OrderApi.Environment
import io.github.g4lowy.order.domain.repository.OrderRepository
import io.github.g4lowy.product.domain.repository.ProductRepository
import org.http4s.HttpRoutes
import zio.{ &, RIO, Runtime, ZIO }

import java.util.UUID

class OrderApi extends OrdersHandler[RIO[AppEnvironment, *]] {
  override def createOrder(respond: CreateOrderResponse.type)(
    body: CreateOrder
  ): RIO[Environment, CreateOrderResponse] = ???

  override def getAllOrders(
    respond: GetAllOrdersResponse.type
  )(offset: Option[Int], limit: Option[Int]): RIO[Environment, GetAllOrdersResponse] = ???
//    val offsetV = offset.getOrElse(DEFAULT_OFFSET)
//    val limitV  = limit.getOrElse(DEFAULT_LIMIT)
//
//    OrderService.getOrders(offsetV, limitV).map(_.map(_.toAPI))

  override def getOrderById(respond: GetOrderByIdResponse.type)(orderId: UUID): RIO[Environment, GetOrderByIdResponse] =
    ???

  override def patchOrder(
    respond: PatchOrderResponse.type
  )(orderId: UUID, body: PatchOrder): RIO[Environment, PatchOrderResponse] = ???
}

object OrderApi {
  type Environment = OrderRepository & ProductRepository

  val routes: RIO[AppEnvironment, HttpRoutes[RIO[AppEnvironment, *]]] = {
    import zio.interop.catz._

    ZIO
      .runtime[AppEnvironment]
      .map { implicit r: Runtime[AppEnvironment] =>
        new OrdersResource[RIO[AppEnvironment, *]]
      }
      .map(_.routes(new OrderApi))
  }
}
