package io.github.g4lowy.order.application

import io.github.g4lowy.broker.{MessageConsumer, MessageProducer}
import io.github.g4lowy.customer.domain.repository.CustomerRepository
import io.github.g4lowy.order.application.broker.{OrderRequestMessage, OrderResponseMessage}
import io.github.g4lowy.order.domain.repository.OrderRepository
import io.github.g4lowy.product.domain.repository.ProductRepository
import zio.{&, Scope, ZIO}

object OrderProcessor {

  def consumeRequests: ZIO[MessageConsumer[OrderRequestMessage] & MessageProducer[
    OrderResponseMessage
  ] & OrderRepository & CustomerRepository & ProductRepository & Scope, Nothing, Unit] = ZIO.scoped {
    ZIO.serviceWithZIO[MessageConsumer[OrderRequestMessage]](_.consume(consumeOrderRequestMessage))
  }

  private def consumeOrderRequestMessage(message: OrderRequestMessage) =
    for {
      result <- OrderService.createOrder(message.value)
      _ <- ZIO.serviceWith[MessageProducer[OrderResponseMessage]](
        _.produce(OrderResponseMessage(message.requestId, result))
      )
    } yield ()
}
