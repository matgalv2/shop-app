package io.github.g4lowy.http.converters

import http.generated.definitions.{ CreateProduct, GetProduct, UpdateProduct }
import io.github.g4lowy.product.domain.model.{ Description, Name, Price, Product, ProductId }
import io.github.g4lowy.product.domain
import io.scalaland.chimney.dsl._

object products {
  implicit class CreateProductOps(private val create: CreateProduct) extends AnyVal {
    def toDomain: Product.Unvalidated =
      create
        .into[Product.Unvalidated]
        .withFieldConst(_.productId, ProductId.generate)
        .withFieldComputed(_.name, x => Name.Unvalidated(x.name))
        .withFieldComputed(_.description, _.description.map(Description.Unvalidated.apply))
        .withFieldComputed(_.price, x => Price.Unvalidated(x.price))
        .transform
  }

  implicit class UpdateProductOps(private val update: UpdateProduct) extends AnyVal {
    def toDomain: Product.Unvalidated =
      update
        .into[Product.Unvalidated]
        .withFieldConst(_.productId, ProductId.generate)
        .withFieldComputed(_.name, x => Name.Unvalidated(x.name))
        .withFieldComputed(_.description, _.description.map(Description.Unvalidated.apply))
        .withFieldComputed(_.price, x => Price.Unvalidated(x.price))
        .transform
  }

  implicit class ProductOps(private val product: Product) extends AnyVal {
    def toAPI: GetProduct =
      product
        .into[GetProduct]
        .withFieldComputed(_.productId, _.productId.value)
        .withFieldComputed(_.name, _.name.value)
        .withFieldComputed(_.description, _.description.map(_.value))
        .withFieldComputed(_.price, _.price.value)
        .transform
  }

  implicit class ProductIdOps(private val productId: domain.model.ProductId) extends AnyVal {
    def toAPI: http.generated.definitions.ProductId =
      productId
        .into[http.generated.definitions.ProductId]
        .withFieldRenamed(_.value, _.productId)
        .transform
  }
}
