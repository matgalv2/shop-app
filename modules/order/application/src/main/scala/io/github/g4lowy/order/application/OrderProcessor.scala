package io.github.g4lowy.order.application

import io.github.g4lowy.broker.application.{MessageConsumer, MessageProducer}
import io.github.g4lowy.customer.domain.repository.CustomerRepository
import io.github.g4lowy.order.application.broker.{OrderRequestMessage, OrderResponseMessage}
import io.github.g4lowy.order.domain.repository.OrderRepository
import io.github.g4lowy.product.domain.repository.ProductRepository
import zio.kafka.consumer.Consumer
import zio.kafka.producer.Producer
import zio.{&, ZIO}

object OrderProcessor {

  type OrderRequestConsumerType = MessageConsumer[OrderRequestMessage, Consumer & Producer & MessageProducer[
    OrderResponseMessage,
    Producer
  ] & OrderRepository & CustomerRepository & ProductRepository, Nothing, Unit]

  def startConsumingOrderRequests = ZIO.scoped {
    ZIO.serviceWithZIO[OrderRequestConsumerType](_.startConsuming)

  }
}
