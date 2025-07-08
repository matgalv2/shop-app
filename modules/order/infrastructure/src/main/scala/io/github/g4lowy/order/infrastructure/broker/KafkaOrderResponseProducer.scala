package io.github.g4lowy.order.infrastructure.broker

import io.github.g4lowy.broker.application.MessageProducer
import io.github.g4lowy.broker.infrastructure.BrokerConfiguration.BOOSTRAP_SERVERS
import io.github.g4lowy.order.application.broker.OrderResponseMessage
import io.github.g4lowy.order.infrastructure.broker.config.KafkaOrderConfig
import org.apache.kafka.clients.producer.ProducerRecord
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.{Serde, Serializer}
import zio.{UIO, ULayer, ZIO, ZLayer}

case class KafkaOrderResponseProducer(
  keySerializer: Serializer[Any, Int],
  valueSerializer: Serializer[Any, OrderResponseMessage]
) extends MessageProducer[OrderResponseMessage] {

  override def produce(value: OrderResponseMessage): UIO[Unit] = ZIO.scoped {
    for {
      producer <- Producer.make(ProducerSettings(BOOSTRAP_SERVERS))
      _ <- producer
        .produce(
          new ProducerRecord(KafkaOrderConfig.KAFKA_ORDER_RESPONSES_TOPIC, value),
          keySerializer,
          valueSerializer
        )
    } yield ()
  }.orDie
}
object KafkaOrderResponseProducer {

  val toLayer: ULayer[MessageProducer[OrderResponseMessage]] = ZLayer.succeed {
    KafkaOrderResponseProducer(Serde.int, OrderCodecs.orderResponseMessageSerde)
  }
}
