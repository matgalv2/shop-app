package io.github.g4lowy.order.infrastructure.broker

import io.github.g4lowy.broker.{BrokerBootstrapServers, MessageProducer}
import io.github.g4lowy.order.application.broker.OrderRequestMessage
import io.github.g4lowy.order.infrastructure.broker.KafkaOrderRequestProducer.TOPIC
import org.apache.kafka.clients.producer.ProducerRecord
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.{Serde, Serializer}
import zio.{UIO, URLayer, ZIO, ZLayer}

case class KafkaOrderRequestProducer(
  keySerializer: Serializer[Any, Int],
  valueSerializer: Serializer[Any, OrderRequestMessage],
  bootstrapServers: BrokerBootstrapServers
) extends MessageProducer[OrderRequestMessage] {

  override def produce(value: OrderRequestMessage): UIO[Unit] = ZIO.scoped {
    for {
      producer <- Producer.make(ProducerSettings(bootstrapServers.value))
      _ <- producer
        .produce(new ProducerRecord(TOPIC, value), keySerializer, valueSerializer)
        .unit
        .orDie
    } yield ()
  }.orDie

}

object KafkaOrderRequestProducer {

  private val TOPIC: String = "order-requests"

  val toLayer: URLayer[BrokerBootstrapServers, KafkaOrderRequestProducer] =
    ZLayer.fromFunction(KafkaOrderRequestProducer(Serde.int, OrderCodecs.orderRequestMessageSerde, _))
}
