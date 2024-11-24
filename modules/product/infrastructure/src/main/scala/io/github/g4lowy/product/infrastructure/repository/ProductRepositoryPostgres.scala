package io.github.g4lowy.product.infrastructure.repository

import io.getquill.CamelCase
import io.getquill.jdbczio.Quill
import io.github.g4lowy.product.domain.model
import io.github.g4lowy.product.domain.model.{ProductError, ProductId}
import io.github.g4lowy.product.domain.repository.ProductRepository
import io.github.g4lowy.product.infrastructure.model.ProductSQL
import zio.{IO, UIO, URLayer, ZIO, ZLayer}

case class ProductRepositoryPostgres(quill: Quill.Postgres[CamelCase]) extends ProductRepository {

  import quill._

  private val products = quote {
    querySchema[ProductSQL]("Products")
  }

  override def getById(productId: ProductId): IO[ProductError.NotFound, model.Product] = {
    val result =
      run {
        quote {
          products.filter(_.productId == lift(productId.value))
        }
      }
    result
      .orDieWith(error => error)
      .flatMap(_.headOption match {
        case Some(value) => ZIO.succeed(value.toDomain)
        case None        => ZIO.fail(ProductError.NotFound(productId))
      })
  }

  override def getAll: UIO[List[model.Product]] = {
    val result = run(quote(products))

    result
      .map(_.map(_.toDomain))
      .orDieWith(error => error)
  }

  override def create(product: model.Product): UIO[ProductId] = {
    val result =
      run {
        quote {
          products.insertValue(lift(ProductSQL.fromDomain(product)))
        }
      }

    result
      .as(product.productId)
      .orDieWith(error => error)
  }

  override def update(productId: ProductId, product: model.Product): IO[ProductError.NotFound, Unit] = {
    val result = run {
      quote {
        products
          .filter(_.productId == lift(productId.value))
          .update(
            _.name -> lift(product.name.value),
            _.price -> lift(product.price.value),
            _.description -> lift(product.description.map(_.value))
          )
      }
    }

    result
      .orDieWith(error => error)
      .flatMap(rowsNo => ZIO.unless(rowsNo == 1)(ZIO.fail(ProductError.NotFound(productId))))
      .unit
  }

  override def delete(productId: ProductId): IO[ProductError.NotFound, Unit] = {
    val result = run {
      quote {
        products
          .filter(_.productId == lift(productId.value))
          .delete
      }
    }

    result
      .orDieWith(error => error)
      .flatMap(rowsNo => ZIO.fail(ProductError.NotFound(productId)).unless(rowsNo == 1))
      .unit
  }
}
object ProductRepositoryPostgres {
  val live: URLayer[Quill.Postgres[CamelCase], ProductRepositoryPostgres] =
    ZLayer.fromFunction(ProductRepositoryPostgres.apply _)
}
