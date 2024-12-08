package io.github.g4lowy.product.domain.repository

import io.github.g4lowy.abstractType.Id
import io.github.g4lowy.product.domain.model.{Product, ProductError, ProductId}
import zio.macros.accessible
import zio.{IO, UIO}

@accessible
trait ProductRepository {
  def getById(productId: ProductId): IO[ProductError.NotFound, Product]
  def getMany(productIds: List[Id]): IO[ProductError.NotFound, List[Product]]
  def getAll: UIO[List[Product]]
  def create(product: Product): UIO[ProductId]
  def update(productId: ProductId, product: Product): IO[ProductError.NotFound, Unit]
  def delete(productId: ProductId): IO[ProductError.NotFound, Unit]
}
