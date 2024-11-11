package io.github.g4lowy.union.types

sealed trait Union2[+A, +B] extends Union

object Union2 {

  final case class First[A](value: A) extends Union2[A, Nothing] {
    override def map[C](pf: PartialFunction[Any, C]): C = pf(value)
  }
  final case class Second[B](value: B) extends Union2[Nothing, B] {
    override def map[C](pf: PartialFunction[Any, C]): C = pf(value)
  }

}
