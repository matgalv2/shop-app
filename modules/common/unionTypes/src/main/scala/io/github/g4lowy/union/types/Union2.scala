package io.github.g4lowy.union.types

sealed trait Union2[+A, +B]

object Union2 {

  final case class First[A](value: A) extends Union2[A, Nothing]
  final case class Second[B](value: B) extends Union2[Nothing, B]

}
