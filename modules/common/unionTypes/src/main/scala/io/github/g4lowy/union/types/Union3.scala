package io.github.g4lowy.union.types

sealed trait Union3[+A, +B, +C]

object Union3 {

  final case class First[A](value: A) extends Union3[A, Nothing, Nothing]
  final case class Second[B](value: B) extends Union3[Nothing, B, Nothing]
  final case class Third[C](value: C) extends Union3[Nothing, Nothing, C]

}
