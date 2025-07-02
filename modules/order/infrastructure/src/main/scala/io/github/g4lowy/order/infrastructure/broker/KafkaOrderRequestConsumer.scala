package io.github.g4lowy.order.infrastructure.broker

import io.github.g4lowy.broker.application.{MessageConsumer, MessageProducer}
import io.github.g4lowy.customer.domain.repository.CustomerRepository
import io.github.g4lowy.order.application.OrderService
import io.github.g4lowy.order.application.broker.{OrderRequestMessage, OrderResponseMessage}
import io.github.g4lowy.order.domain.repository.OrderRepository
import io.github.g4lowy.order.infrastructure.broker.KafkaOrderRequestConsumer.OrderRequestConsumerType
import io.github.g4lowy.order.infrastructure.broker.config.KafkaOrderConfig
import io.github.g4lowy.product.domain.repository.ProductRepository
import zio.kafka.consumer.{Consumer, Subscription}
import zio.kafka.producer.Producer
import zio.kafka.serde.{Deserializer, Serde}
import zio.{&, ULayer, ZIO, ZLayer}

case class KafkaOrderRequestConsumer(
  keyDeserializer: Deserializer[Any, Int],
  valueDeserializer: Deserializer[Any, OrderRequestMessage]
) extends OrderRequestConsumerType {

  override protected def consume(
    func: OrderRequestMessage => ZIO[Consumer & Producer & MessageProducer[
      OrderResponseMessage,
      Producer
    ] & OrderRepository & CustomerRepository & ProductRepository, Nothing, Unit]
  ): ZIO[Consumer & Producer & MessageProducer[
    OrderResponseMessage,
    Producer
  ] & OrderRepository & CustomerRepository & ProductRepository, Nothing, Unit] = ZIO.scoped {
    ZIO.service[Consumer].flatMap {
      _.plainStream(
        Subscription.topics(KafkaOrderConfig.KAFKA_ORDER_REQUEST_TOPIC),
        keyDeserializer,
        valueDeserializer
      ).orDie
        .tap(record => func(record.value))
        .map(_.offset)
        .aggregateAsync(Consumer.offsetBatches)
        .mapZIO(_.commit.orDie)
        .runDrain
    }
  }

  override protected def func: OrderRequestMessage => ZIO[Consumer & Producer with MessageProducer[
    OrderResponseMessage,
    Producer
  ] with OrderRepository & CustomerRepository & ProductRepository, Nothing, Unit] = (message: OrderRequestMessage) =>
    for {
      result   <- OrderService.createOrder(message.value)
      producer <- ZIO.service[MessageProducer[OrderResponseMessage, Producer]]
      _        <- producer.produce(OrderResponseMessage(message.requestId, result))
    } yield ()
}

object KafkaOrderRequestConsumer {

  type OrderRequestConsumerType = MessageConsumer[OrderRequestMessage, Consumer & Producer & MessageProducer[
    OrderResponseMessage,
    Producer
  ] & OrderRepository & CustomerRepository & ProductRepository, Nothing, Unit]

  val toLayer: ULayer[OrderRequestConsumerType] =
    ZLayer.succeed(KafkaOrderRequestConsumer(Serde.int, OrderCodecs.orderRequestMessageSerde))
}
