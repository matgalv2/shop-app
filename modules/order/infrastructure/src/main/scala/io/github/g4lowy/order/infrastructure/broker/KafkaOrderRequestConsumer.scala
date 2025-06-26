package io.github.g4lowy.order.infrastructure.broker

import io.github.g4lowy.broker.{BrokerBootstrapServers, MessageConsumer}
import io.github.g4lowy.order.application.broker.OrderRequestMessage
import io.github.g4lowy.order.infrastructure.broker.KafkaOrderRequestConsumer.{ORDER_REQUESTS_CONSUMER_GROUP_ID, TOPIC}
import zio.kafka.consumer.{Consumer, ConsumerSettings, Subscription}
import zio.kafka.serde.{Deserializer, Serde}
import zio.{&, Scope, URLayer, ZIO, ZLayer}

case class KafkaOrderRequestConsumer(
  keyDeserializer: Deserializer[Any, Int],
  valueDeserializer: Deserializer[Any, OrderRequestMessage],
  bootstrapServers: BrokerBootstrapServers
) extends MessageConsumer[OrderRequestMessage] {

  override def consume[R1, E, A, R2 <: R1](func: OrderRequestMessage => ZIO[R1, E, A]): ZIO[R2 & Scope, Nothing, Unit] =
    for {
      consumer <- Consumer
        .make(ConsumerSettings(bootstrapServers.value).withGroupId(ORDER_REQUESTS_CONSUMER_GROUP_ID))
        .orDie
      _ <- consumer
        .plainStream(Subscription.topics(TOPIC), keyDeserializer, valueDeserializer)
        .tap(record => func(record.value))
        .orDieWith(err => new RuntimeException(err.toString))
        .map(_.offset)
        .aggregateAsync(Consumer.offsetBatches)
        .mapZIO(_.commit)
        .runDrain
        .orDie
        .as()
    } yield ()

}

object KafkaOrderRequestConsumer {

  val toLayer: URLayer[BrokerBootstrapServers, KafkaOrderRequestConsumer] =
    ZLayer.fromFunction(KafkaOrderRequestConsumer(Serde.int, OrderCodecs.orderRequestMessageSerde, _))

  private val TOPIC: String = "order-requests"

  private val ORDER_REQUESTS_CONSUMER_GROUP_ID = "order-requests-consumer-group-1"
}
