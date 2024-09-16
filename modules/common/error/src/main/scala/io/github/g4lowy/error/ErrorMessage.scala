package io.github.g4lowy.error

/** Type class */
trait ErrorMessage[E] {
  def message(e: E): String
}
object ErrorMessage {

  /**  Summoner method enables ErrorMessage[E1].message(error) instead of
    *  implicitly[ErrorMessage[E1]].message(error) when using context bound.
    */

  def apply[E](implicit errorMessage: ErrorMessage[E]): ErrorMessage[E] = errorMessage

  implicit class ErrorToMessageOps[E: ErrorMessage](private val error: E) {
    def toMessage: String = ErrorMessage[E].message(error)
  }
}
