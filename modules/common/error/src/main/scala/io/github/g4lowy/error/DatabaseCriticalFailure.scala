package io.github.g4lowy.error

final case class DatabaseCriticalFailure(message: String = "Critical problem with executing SQL statements!")
    extends Exception
