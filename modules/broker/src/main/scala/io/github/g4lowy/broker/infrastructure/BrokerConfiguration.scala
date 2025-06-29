package io.github.g4lowy.broker.infrastructure

import zio.ZLayer
import zio.kafka.consumer.{Consumer, ConsumerSettings}
import zio.kafka.producer.{Producer, ProducerSettings}

object BrokerConfiguration {
  private val BOOSTRAP_SERVERS = List("localhost:9092")

  val consumerLayer: ZLayer[Any, Throwable, Consumer] = ZLayer.scoped {
    Consumer.make(ConsumerSettings(BOOSTRAP_SERVERS))
  }
  val producerLayer: ZLayer[Any, Throwable, Producer] = ZLayer.scoped {
    Producer.make(ProducerSettings(BOOSTRAP_SERVERS))
  }

}
