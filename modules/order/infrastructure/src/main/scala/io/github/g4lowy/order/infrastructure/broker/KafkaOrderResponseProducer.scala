package io.github.g4lowy.order.infrastructure.broker

import io.github.g4lowy.broker.application.MessageProducer
import io.github.g4lowy.order.application.broker.OrderResponseMessage
import io.github.g4lowy.order.infrastructure.broker.config.KafkaOrderConfig
import org.apache.kafka.clients.producer.ProducerRecord
import zio.kafka.producer.Producer
import zio.kafka.serde.{Serde, Serializer}
import zio.{ULayer, URIO, ZIO, ZLayer}

case class KafkaOrderResponseProducer(
  keySerializer: Serializer[Any, Int],
  valueSerializer: Serializer[Any, OrderResponseMessage]
) extends MessageProducer[OrderResponseMessage, Producer] {

  override def produce(value: OrderResponseMessage): URIO[Producer, Unit] = ZIO.scoped {
    ZIO.serviceWithZIO[Producer] {
      _.produce(
        new ProducerRecord(KafkaOrderConfig.KAFKA_ORDER_RESPONSES_TOPIC, value),
        keySerializer,
        valueSerializer
      ).orDie.unit
    }
  }
}
object KafkaOrderResponseProducer {

  val toLayer: ULayer[MessageProducer[OrderResponseMessage, Producer]] = ZLayer.succeed {
    KafkaOrderResponseProducer(Serde.int, OrderCodecs.orderResponseMessageSerde)
  }
}
