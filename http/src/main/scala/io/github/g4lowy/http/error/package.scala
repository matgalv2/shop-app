package io.github.g4lowy.http

import io.github.g4lowy.customer.domain.model.CustomerError
import io.github.g4lowy.error.ErrorMessage
import io.github.g4lowy.order.domain.model.OrderError
import io.github.g4lowy.product.domain.model.ProductError
import io.github.g4lowy.validation.validators.Validator

package object error {

  implicit val validationError: ErrorMessage[Validator.FailureDescription] = description =>
    s"Validation failure description: $description"

  implicit val customerErrorNotFound: ErrorMessage[CustomerError.NotFound] = { case CustomerError.NotFound(clientId) =>
    s"Customer with id: $clientId was not found"
  }

  implicit val productErrorNotFound: ErrorMessage[ProductError.NotFound] = {
    case ProductError.NotFound(productId, productIds) =>
      s"Products with ids: [$productId, ${productIds.mkString(",")}] were not found"
  }

  implicit val orderError: ErrorMessage[OrderError] = {
    case error: OrderError.NotFound      => orderErrorNotFound.message(error)
    case error: OrderError.InvalidStatus => orderErrorInvalidStatus.message(error)
  }

  implicit val orderErrorNotFound: ErrorMessage[OrderError.NotFound] = { case OrderError.NotFound(orderId) =>
    s"Order with id: ${orderId.value} was not found"
  }

  implicit val orderErrorInvalidStatus: ErrorMessage[OrderError.InvalidStatus] = {
    case OrderError.InvalidStatus(orderId, status) =>
      s"Status od an order with id: $orderId can not be set to $status"
  }
}
