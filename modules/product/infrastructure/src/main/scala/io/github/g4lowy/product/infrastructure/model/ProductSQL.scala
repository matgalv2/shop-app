package io.github.g4lowy.product.infrastructure.model

import io.github.g4lowy.product.domain.model.{ Description, Name, Price, Product, ProductId }

import java.util.UUID

case class ProductSQL(productId: UUID, name: String, price: Double, description: Option[String]) {
  def toDomain: Product =
    Product
      .Unvalidated(
        productId   = ProductId.fromUUID(productId),
        name        = Name.Unvalidated(name),
        price       = Price.Unvalidated(price),
        description = description.map(Description.Unvalidated.apply)
      )
      .unsafeValidation
}
object ProductSQL {
  def fromDomain(product: Product): ProductSQL =
    ProductSQL(
      productId   = product.productId.value,
      name        = product.name.value,
      price       = product.price.value,
      description = product.description.map(_.value)
    )
}
