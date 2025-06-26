package io.github.g4lowy.product.application

import io.github.g4lowy.abstracttype.Id
import io.github.g4lowy.product.domain.model.{Product, ProductError, ProductId}
import io.github.g4lowy.product.domain.repository.ProductRepository
import zio.{URIO, ZIO}

object ProductService {

  private val DEFAULT_OFFSET = 0
  private val DEFAULT_LIMIT  = 10

  def getProducts(offset: Option[Int], limit: Option[Int]): URIO[ProductRepository, List[Product]] =
    ProductRepository.getAll(offset.getOrElse(DEFAULT_OFFSET), limit.getOrElse(DEFAULT_LIMIT))

  def getMany(ids: List[Id]): ZIO[ProductRepository, ProductError.NotFound, List[Product]] =
    ProductRepository.getMany(ids.map(id => ProductId.fromUUID(id.value)))

  def getProductById(id: Id): ZIO[ProductRepository, ProductError.NotFound, Product] =
    ProductRepository.getById(ProductId.fromUUID(id.value))

  def createProduct(product: Product): URIO[ProductRepository, ProductId] = ProductRepository.create(product)

  def updateProduct(id: Id, product: Product): ZIO[ProductRepository, ProductError.NotFound, Unit] =
    ProductRepository.update(ProductId.fromUUID(id.value), product)

  def deleteProduct(id: Id): ZIO[ProductRepository, ProductError.NotFound, Unit] =
    ProductRepository.delete(ProductId.fromUUID(id.value))
}
