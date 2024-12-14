package io.github.g4lowy.http.service

import io.github.g4lowy.product.domain.model.{Product, ProductError, ProductId}
import io.github.g4lowy.product.domain.repository.ProductRepository
import zio.{URIO, ZIO}

object ProductService {

  private val DEFAULT_OFFSET = 0
  private val DEFAULT_LIMIT = 10

  def getProducts(offset: Option[Int], limit: Option[Int]): URIO[ProductRepository, List[Product]] =
    ProductRepository.getAll(offset.getOrElse(DEFAULT_OFFSET), limit.getOrElse(DEFAULT_LIMIT))

  def getMany(ids: List[ProductId]): ZIO[ProductRepository, ProductError.NotFound, List[Product]] =
    ProductRepository.getMany(ids)

  def getProductById(productId: ProductId): ZIO[ProductRepository, ProductError.NotFound, Product] =
    ProductRepository.getById(productId)

  def createProduct(product: Product): URIO[ProductRepository, ProductId] = ProductRepository.create(product)

  def updateProduct(productId: ProductId, product: Product): ZIO[ProductRepository, ProductError.NotFound, Unit] =
    ProductRepository.update(productId, product)

  def deleteProduct(productId: ProductId): ZIO[ProductRepository, ProductError.NotFound, Unit] =
    ProductRepository.delete(productId)
}
