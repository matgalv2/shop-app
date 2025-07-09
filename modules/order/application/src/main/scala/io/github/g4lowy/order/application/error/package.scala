package io.github.g4lowy.order.application

import io.github.g4lowy.error.ErrorMessage
import io.github.g4lowy.order.application.ApplicationOrderError.{CustomerNotFound, ProductsNotFound}
import io.github.g4lowy.validation.validators.ValidationFailure

package object error {
  implicit val validationError: ErrorMessage[ValidationFailure] = description =>
    s"Validation failure description: $description"

  implicit val customerNotFoundError: ErrorMessage[CustomerNotFound] = { case CustomerNotFound(customerId) =>
    s"Customer with id: $customerId was not found"
  }

  implicit val productsNotFound: ErrorMessage[ProductsNotFound] = { case ProductsNotFound(productId, productsIds) =>
    s"Products with ids: [$productId, ${productsIds.mkString(",")}] were not found"
  }

  implicit val applicationOrderError: ErrorMessage[ApplicationOrderError] = {
    case error: CustomerNotFound => customerNotFoundError.message(error)
    case error: ProductsNotFound => productsNotFound.message(error)
  }
}
