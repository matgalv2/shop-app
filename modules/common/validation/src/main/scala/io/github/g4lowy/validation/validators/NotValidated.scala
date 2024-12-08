package io.github.g4lowy.validation.validators

trait NotValidated[A] {
  def validate: Validation[Validator.FailureDescription, A]
  protected def unsafeValidation: A
}
