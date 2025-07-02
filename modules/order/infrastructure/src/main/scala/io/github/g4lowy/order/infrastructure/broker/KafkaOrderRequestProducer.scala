package io.github.g4lowy.order.infrastructure.broker

import io.github.g4lowy.broker.application.MessageProducer
import io.github.g4lowy.order.application.broker.OrderRequestMessage
import io.github.g4lowy.order.infrastructure.broker.config.KafkaOrderConfig
import org.apache.kafka.clients.producer.ProducerRecord
import zio.kafka.producer.Producer
import zio.kafka.serde.{Serde, Serializer}
import zio.{ULayer, URIO, ZIO, ZLayer}

case class KafkaOrderRequestProducer(
  keySerializer: Serializer[Any, Int],
  valueSerializer: Serializer[Any, OrderRequestMessage]
) extends MessageProducer[OrderRequestMessage, Producer] {

  override def produce(value: OrderRequestMessage): URIO[Producer, Unit] = ZIO.scoped {
    ZIO.serviceWithZIO[Producer] {
      _.produce(
        new ProducerRecord(KafkaOrderConfig.KAFKA_ORDER_REQUEST_TOPIC, value),
        keySerializer,
        valueSerializer
      ).orDie.unit
    }
  }
}

object KafkaOrderRequestProducer {

  val toLayer: ULayer[MessageProducer[OrderRequestMessage, Producer]] = ZLayer.succeed {
    KafkaOrderRequestProducer(Serde.int, OrderCodecs.orderRequestMessageSerde)
  }
}
