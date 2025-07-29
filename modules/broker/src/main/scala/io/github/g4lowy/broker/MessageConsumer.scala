package io.github.g4lowy.broker

import zio.{&, Scope, ZIO}

trait MessageConsumer[M <: Message[_]] {

  def consume[R1, E, A, R2 <: R1](func: M => ZIO[R1, E, A]): ZIO[R2 & Scope, Nothing, Unit]
}
