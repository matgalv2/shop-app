package io.github.g4lowy.test.utils

import io.github.g4lowy.validation.validators.Validation

package object validation {

  implicit class ValidationOps[A, B](private val validation: Validation[B, A]) extends AnyVal {

    /** Only for test purposes.
      * @return the valid object of this validation.
      */
    def asValid: A = validation match {
      case Validation.Valid(value) => value
      case Validation.Invalid(_)   => throw new RuntimeException("Validation failed")
    }
  }
}
