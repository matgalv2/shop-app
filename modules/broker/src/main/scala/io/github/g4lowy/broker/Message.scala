package io.github.g4lowy.broker

trait Message[V] {

  val value: V
}
