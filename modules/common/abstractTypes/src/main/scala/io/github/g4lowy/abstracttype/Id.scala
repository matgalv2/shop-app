package io.github.g4lowy.abstracttype

import java.util.UUID

trait Id { self =>
  val value: UUID

  override def toString: String = s"Id($value)"

  override def equals(obj: Any): Boolean =
    obj match {
      case that: Id => self.value == that.value
      case _        => false
    }

  override def hashCode(): Int = value.hashCode()
}

object Id {

  implicit class UUIDOps(private val uuid: UUID) extends AnyVal {
    def toId: Id = new Id {
      override val value: UUID = uuid
    }
  }
}
