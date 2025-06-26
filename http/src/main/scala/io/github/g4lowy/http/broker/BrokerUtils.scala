package io.github.g4lowy.http.broker

import io.github.g4lowy.broker.BrokerBootstrapServers
import io.github.g4lowy.http.AppConfig
import io.github.g4lowy.order.infrastructure.broker.{KafkaOrderRequestConsumer, KafkaOrderRequestProducer, KafkaOrderResponseProducer}
import zio.{URLayer, ZLayer}

object BrokerUtils {

  val kafkaBootstrapServers: URLayer[AppConfig, BrokerBootstrapServers] =
    ZLayer.fromFunction((config: AppConfig) => BrokerBootstrapServers(config.broker.bootstrapServers))

  val kafkaConsumersLive: URLayer[BrokerBootstrapServers, KafkaOrderRequestConsumer] = KafkaOrderRequestConsumer.toLayer

  val kafkaProducersLive: URLayer[BrokerBootstrapServers, KafkaOrderRequestProducer with KafkaOrderResponseProducer] =
    KafkaOrderRequestProducer.toLayer ++ KafkaOrderResponseProducer.toLayer
}
