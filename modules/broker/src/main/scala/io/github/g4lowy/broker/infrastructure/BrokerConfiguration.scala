package io.github.g4lowy.broker.infrastructure

import zio.ZLayer
import zio.kafka.consumer.{Consumer, ConsumerSettings}
import zio.kafka.producer.{Producer, ProducerSettings}

object BrokerConfiguration {

  private val BOOSTRAP_SERVERS = List("192.168.1.117:9092")

  private val ORDER_CONSUMER_GROUP_ID = "order-requests-consumer-group-1"

  val consumerLayer: ZLayer[Any, Throwable, Consumer] = ZLayer.scoped {
    Consumer.make(ConsumerSettings(BOOSTRAP_SERVERS).withGroupId(ORDER_CONSUMER_GROUP_ID))
  }

  val producerLayer: ZLayer[Any, Throwable, Producer] = ZLayer.scoped {
    Producer.make(ProducerSettings(BOOSTRAP_SERVERS))
  }
}
