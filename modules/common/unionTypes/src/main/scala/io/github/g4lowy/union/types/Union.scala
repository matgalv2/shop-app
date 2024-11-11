package io.github.g4lowy.union.types

private[types] trait Union {
  def map[AA](pf: PartialFunction[Any, AA]): AA
}
