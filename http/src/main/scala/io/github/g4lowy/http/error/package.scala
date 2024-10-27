package io.github.g4lowy.http

import io.github.g4lowy.customer.domain.model.CustomerError
import io.github.g4lowy.product.domain.model.ProductError
import io.github.g4lowy.error.ErrorMessage
import io.github.g4lowy.validation.validators.Validator

package object error {

  implicit val validation: ErrorMessage[Validator.FailureDescription] = description =>
    s"Validation failure description: $description"

  implicit val customerNotFound: ErrorMessage[CustomerError.NotFound] = { case CustomerError.NotFound(clientId) =>
    s"Client with id: $clientId was not found"
  }

  implicit val productNotFound: ErrorMessage[ProductError.NotFound] = { case ProductError.NotFound(productId) =>
    s"Product with id: $productId was not found"
  }
}
