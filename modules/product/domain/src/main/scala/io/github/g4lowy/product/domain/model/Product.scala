package io.github.g4lowy.product.domain.model

import io.github.g4lowy.validation.validators.Validator.{ alwaysValid, validOrCheck, FailureDescription }
import io.github.g4lowy.validation.validators.{ NotValidated, Validation, Validator }

final case class Product private (productId: ProductId, name: Name, price: Price, description: Option[Description])

object Product {

  final case class Unvalidated(
    productId: ProductId.Unvalidated,
    name: Name.Unvalidated,
    price: Price.Unvalidated,
    description: Option[Description.Unvalidated]
  ) extends NotValidated[Product] {

    override def validate: Validation[FailureDescription, Product] =
      for {
        id          <- productId.validate
        name        <- name.validate
        price       <- price.validate
        description <- validOrCheck(description)
      } yield Product(id, name, price, description)

    override def unsafeValidation: Product = Product(
      productId   = productId.unsafeValidation,
      name        = name.unsafeValidation,
      price       = price.unsafeValidation,
      description = description.map(_.unsafeValidation)
    )
  }
}
