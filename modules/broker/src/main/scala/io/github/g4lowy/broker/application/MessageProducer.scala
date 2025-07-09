package io.github.g4lowy.broker.application

import zio.UIO

trait MessageProducer[M <: Message[_]] {

  def produce(value: M): UIO[Unit]

}
