package io.github.g4lowy.order.application.broker

import io.github.g4lowy.order.application.Result
import io.github.g4lowy.order.application.dto.OrderDto
import zio.macros.accessible
import zio.{URIO, ZIO}

@accessible
trait OrderProcessor {

  def produce[R](orderDto: OrderDto): URIO[R, Unit]

  def consume[R1, R2](effect: URIO[R1, Result]): ZIO[R2, Nothing, Unit]
}
