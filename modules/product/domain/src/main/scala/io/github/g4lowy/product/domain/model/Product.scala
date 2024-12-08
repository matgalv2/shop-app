package io.github.g4lowy.product.domain.model

import io.github.g4lowy.validation.validators.Validator.{FailureDescription, validOrCheck}
import io.github.g4lowy.validation.validators.{NotValidated, Validation}

final case class Product private (productId: ProductId, name: Name, price: Price, description: Option[Description])

object Product {

  final case class Unvalidated(
    productId: ProductId,
    name: Name.Unvalidated,
    price: Price.Unvalidated,
    description: Option[Description.Unvalidated]
  ) extends NotValidated[Product] {

    override def validate: Validation[FailureDescription, Product] =
      for {
        name        <- name.validate
        price       <- price.validate
        description <- validOrCheck[Description, Description.Unvalidated](description)
      } yield Product(productId, name, price, description)

    override protected def unsafeValidation: Product = Product(
      productId   = productId,
      name        = name.validateUnsafe,
      price       = price.validateUnsafe,
      description = description.map(_.validateUnsafe)
    )

    private[product] def validateUnsafe: Product = unsafeValidation
  }
}
