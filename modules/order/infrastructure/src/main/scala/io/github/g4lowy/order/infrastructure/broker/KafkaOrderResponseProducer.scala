package io.github.g4lowy.order.infrastructure.broker

import io.github.g4lowy.broker.{BrokerBootstrapServers, MessageProducer}
import io.github.g4lowy.order.application.broker.OrderResponseMessage
import io.github.g4lowy.order.infrastructure.broker.KafkaOrderResponseProducer.TOPIC
import org.apache.kafka.clients.producer.ProducerRecord
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.{Serde, Serializer}
import zio.{UIO, URLayer, ZIO, ZLayer}

case class KafkaOrderResponseProducer(
  keySerializer: Serializer[Any, Int],
  valueSerializer: Serializer[Any, OrderResponseMessage],
  bootstrapServers: BrokerBootstrapServers
) extends MessageProducer[OrderResponseMessage] {

  override def produce(value: OrderResponseMessage): UIO[Unit] = ZIO.scoped {
    for {
      producer <- Producer.make(ProducerSettings(bootstrapServers.value))
      _ <- producer
        .produce(new ProducerRecord(TOPIC, value), keySerializer, valueSerializer)
    } yield ()
  }.orDie

}
object KafkaOrderResponseProducer {

  private val TOPIC: String = "order-responses"

  val toLayer: URLayer[BrokerBootstrapServers, KafkaOrderResponseProducer] =
    ZLayer.fromFunction(KafkaOrderResponseProducer(Serde.int, OrderCodecs.orderResponseMessageSerde, _))
}
