package io.github.g4lowy.order.infrastructure.broker

import io.github.g4lowy.broker.application.MessageProducer
import io.github.g4lowy.broker.infrastructure.BrokerConfiguration.BOOSTRAP_SERVERS
import io.github.g4lowy.order.application.broker.OrderRequestMessage
import io.github.g4lowy.order.infrastructure.broker.config.KafkaOrderConfig
import org.apache.kafka.clients.producer.ProducerRecord
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.{Serde, Serializer}
import zio.{UIO, ULayer, ZIO, ZLayer}

case class KafkaOrderRequestProducer(
  keySerializer: Serializer[Any, Int],
  valueSerializer: Serializer[Any, OrderRequestMessage]
) extends MessageProducer[OrderRequestMessage] {

  override def produce(value: OrderRequestMessage): UIO[Unit] = ZIO.scoped {
    for {
      producer <- Producer.make(ProducerSettings(BOOSTRAP_SERVERS))
      _ <- producer
        .produce(new ProducerRecord(KafkaOrderConfig.KAFKA_ORDER_REQUEST_TOPIC, value), keySerializer, valueSerializer)
        .unit
        .orDie
    } yield ()
  }.orDie
}

object KafkaOrderRequestProducer {

  val toLayer: ULayer[MessageProducer[OrderRequestMessage]] = ZLayer.succeed {
    KafkaOrderRequestProducer(Serde.int, OrderCodecs.orderRequestMessageSerde)
  }
}
