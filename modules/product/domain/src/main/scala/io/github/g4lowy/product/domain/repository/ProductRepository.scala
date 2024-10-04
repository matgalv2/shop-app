package io.github.g4lowy.product.domain.repository

import io.github.g4lowy.product.domain.model.{ Product, ProductError, ProductId }
import zio.{ IO, UIO }
import zio.macros.accessible

@accessible
trait ProductRepository {
  def getById(productId: ProductId): IO[ProductError.NotFound, Product]
  def getAll: UIO[List[Product]]
  def create(product: Product): UIO[ProductId]
  def update(productId: ProductId, product: Product): IO[ProductError.NotFound, Unit]
  def delete(productId: ProductId): IO[ProductError.NotFound, Unit]
}
