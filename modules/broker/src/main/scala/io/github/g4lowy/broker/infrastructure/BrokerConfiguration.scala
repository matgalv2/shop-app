package io.github.g4lowy.broker.infrastructure

object BrokerConfiguration {

  val BOOSTRAP_SERVERS = List("192.168.1.117:9092")

//  val consumerLayer: ZLayer[Any, Throwable, Consumer] = ZLayer.scoped {
//    Consumer.make(ConsumerSettings(BOOSTRAP_SERVERS).withGroupId(ORDER_CONSUMER_GROUP_ID))
//  }
}
