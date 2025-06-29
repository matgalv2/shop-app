package io.github.g4lowy.broker.application

import zio.URIO

trait MessageProducer[V, R] {

  def produce(value: V): URIO[R, Unit]

}
