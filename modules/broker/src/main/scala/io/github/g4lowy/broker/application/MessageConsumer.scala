package io.github.g4lowy.broker.application

import zio.ZIO

trait MessageConsumer[MV <: Message[_], R, E, A] {

  protected def consume(func: MV => ZIO[R, E, A]): ZIO[R, Nothing, Unit]

  // The function to process messages is now stored here
  protected def func: MV => ZIO[R, E, A]

  final def startConsuming: ZIO[R, Nothing, Unit] = consume(func)

}
