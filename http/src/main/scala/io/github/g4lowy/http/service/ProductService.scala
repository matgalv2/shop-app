package io.github.g4lowy.http.service

import http.generated.definitions.{ CreateProduct, GetProduct, UpdateProduct }
import io.github.g4lowy.http.converters.products._
import io.github.g4lowy.product.domain.repository.ProductRepository
import io.github.g4lowy.product.domain.model.{ Product, ProductError, ProductId }
import io.github.g4lowy.union.types.Union2
import io.github.g4lowy.validation.extras.ZIOValidationOps
import io.github.g4lowy.validation.validators.Validator
import zio.{ RIO, URIO, ZIO }

import java.util.UUID

object ProductService {
  def getProducts: URIO[ProductRepository, Vector[GetProduct]] =
    ProductRepository.getAll
      .map(_.map(_.toAPI))
      .map(_.toVector)

  def getProductById(productId: UUID): ZIO[ProductRepository, ProductError.NotFound, Product] = {
    val domainId = ProductId.fromUUID(productId)
    ProductRepository.getById(domainId)
  }

  def createProduct(createProduct: CreateProduct): ZIO[ProductRepository, Validator.FailureDescription, ProductId] =
    ZIO
      .fromNotValidated(createProduct.toDomain)
      .flatMap(ProductRepository.create)

  def updateProduct(
    productId: UUID,
    updateProduct: UpdateProduct
  ): ZIO[ProductRepository, Union2[Validator.FailureDescription, ProductError.NotFound], Unit] =
    ZIO
      .fromNotValidated(updateProduct.toDomain)
      .mapError(Union2.First.apply)
      .flatMap { product =>
        val domainId = ProductId.fromUUID(productId)
        ProductRepository
          .update(domainId, product)
          .mapError(Union2.Second.apply)
      }

  def deleteProduct(productId: UUID): ZIO[ProductRepository, ProductError.NotFound, Unit] = {
    val domainId = ProductId.fromUUID(productId)
    ProductRepository.delete(domainId)
  }
}
