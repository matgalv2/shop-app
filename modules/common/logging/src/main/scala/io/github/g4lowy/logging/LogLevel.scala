package io.github.g4lowy.logging

sealed trait LogLevel

object LogLevel {
  final case object Info extends LogLevel
  final case object Warn extends LogLevel
  final case object Error extends LogLevel
}
