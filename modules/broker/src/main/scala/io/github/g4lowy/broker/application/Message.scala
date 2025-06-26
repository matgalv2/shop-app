package io.github.g4lowy.broker.application

trait Message[V] {

  val value: V
}
