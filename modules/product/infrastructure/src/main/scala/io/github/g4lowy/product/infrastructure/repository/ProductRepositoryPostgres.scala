package io.github.g4lowy.product.infrastructure.repository

import io.getquill.CamelCase
import io.getquill.jdbczio.Quill
import io.github.g4lowy.abstracttype.Id.UUIDOps
import io.github.g4lowy.product.domain.model
import io.github.g4lowy.product.domain.model.{Product, ProductError, ProductId}
import io.github.g4lowy.product.domain.repository.ProductRepository
import io.github.g4lowy.product.infrastructure.model.ProductSQL
import zio.{IO, UIO, URLayer, ZIO, ZLayer}

import java.sql.SQLException

case class ProductRepositoryPostgres(quill: Quill.Postgres[CamelCase]) extends ProductRepository {

  import quill._

  private val products = quote {
    querySchema[ProductSQL]("Products")
  }

  private def productsOffsetAndLimit(offset: Int, limit: Int) =
    quote {
      querySchema[ProductSQL]("Products").drop(lift(offset)).take(lift(limit))
    }

  override def getById(productId: ProductId): IO[ProductError.NotFound, model.Product] = {
    val result =
      run {
        quote {
          products.filter(_.productId == lift(productId.value))
        }
      }

    result.orDie
      .flatMap(_.headOption match {
        case Some(value) => ZIO.succeed(value.toDomain.validateUnsafe)
        case None        => ZIO.fail(ProductError.NotFound(productId, Nil))
      })
  }

  override def getAll(offset: Index, limit: Index): UIO[List[Product]] = {
    val result = run(quote(productsOffsetAndLimit(offset, limit)))

    result
      .map(_.map(_.toDomain.validateUnsafe))
      .orDie
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

    result.orDie
      .flatMap(rowsNo => ZIO.unless(rowsNo == 1)(ZIO.fail(ProductError.NotFound(productId, Nil))))
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

    result.orDie
      .flatMap(rowsNo => ZIO.fail(ProductError.NotFound(productId, Nil)).unless(rowsNo == 1))
      .unit
  }

  override def getMany(productIds: List[ProductId]): IO[ProductError.NotFound, List[Product]] = {
    val ids = productIds.map(_.value)
    val result: ZIO[Any, SQLException, List[ProductSQL]] = run {
      quote {
        products
          .filter(product => liftQuery(ids).contains(product.productId))
      }
    }
    result.orDie.flatMap { foundProducts =>
      val foundIds = foundProducts.map(_.productId.toId)
      productIds.diff(foundIds) match {
        case Nil          => ZIO.succeed(foundProducts.map(_.toDomain.validateUnsafe))
        case head :: tail => ZIO.fail(ProductError.NotFound(head, tail))
      }
    }
  }
}
object ProductRepositoryPostgres {
  val live: URLayer[Quill.Postgres[CamelCase], ProductRepositoryPostgres] =
    ZLayer.fromFunction(ProductRepositoryPostgres.apply _)
}
