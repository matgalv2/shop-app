package io.github.g4lowy.http.api

import http.generated.definitions.{ CreateProduct, ErrorResponse, UpdateProduct }
import http.generated.products.{
  CreateProductResponse,
  DeleteProductResponse,
  GetAllProductsResponse,
  GetProductByIdResponse,
  ProductsHandler,
  ProductsResource,
  UpdateProductResponse
}
import io.github.g4lowy.error.ErrorMessage._
import io.github.g4lowy.http.AppEnvironment
import io.github.g4lowy.http.api.ProductApi.Environment
import io.github.g4lowy.http.service.ProductService
import io.github.g4lowy.product.domain.repository.ProductRepository
import zio.{ RIO, Runtime, ZIO }
import io.github.g4lowy.http.converters.products._
import io.github.g4lowy.http.error._
import io.github.g4lowy.product.domain.model.ProductId
import io.github.g4lowy.validation.extras.ZIOValidationOps
import org.http4s.HttpRoutes

import java.util.UUID

class ProductApi extends ProductsHandler[RIO[AppEnvironment, *]] {

  override def createProduct(
    respond: CreateProductResponse.type
  )(body: CreateProduct): RIO[Environment, CreateProductResponse] =
    ZIO
      .fromNotValidated(body.toDomain)
      .mapError(error => respond.BadRequest(ErrorResponse.single(error.toMessage)))
      .flatMap(ProductService.createProduct)
      .map(productId => respond.Created(productId.value))
      .merge

  override def getAllProducts(respond: GetAllProductsResponse.type)(): RIO[Environment, GetAllProductsResponse] =
    ProductService.getProducts
      .map(_.map(_.toAPI))
      .map(_.toVector)
      .map(respond.Ok)

  override def getProductById(
    respond: GetProductByIdResponse.type
  )(productId: UUID): RIO[Environment, GetProductByIdResponse] =
    ZIO
      .fromNotValidated(ProductId.fromUUID(productId))
      .mapError(error => respond.BadRequest(ErrorResponse.single(error.toMessage)))
      .flatMap { productId =>
        ProductService
          .getProductById(productId)
          .mapError(error => respond.NotFound(ErrorResponse.single(error.toMessage)))
      }
      .map(_.toAPI)
      .map(respond.Ok)
      .merge

  override def deleteProduct(
    respond: DeleteProductResponse.type
  )(productId: UUID): RIO[Environment, DeleteProductResponse] =
    ZIO
      .fromNotValidated(ProductId.fromUUID(productId))
      .mapError(error => respond.BadRequest(ErrorResponse.single(error.toMessage)))
      .flatMap { id =>
        ProductService
          .deleteProduct(id)
          .mapError(error => respond.NotFound(ErrorResponse.single(error.toMessage)))
      }
      .as(respond.NoContent)
      .merge

  override def updateProduct(
    respond: UpdateProductResponse.type
  )(productId: UUID, body: UpdateProduct): RIO[Environment, UpdateProductResponse] =
    ZIO
      .fromNotValidated(body.toDomain)
      .mapError(error => respond.BadRequest(ErrorResponse.single(error.toMessage)))
      .flatMap(product =>
        ZIO
          .fromNotValidated(ProductId.fromUUID(productId))
          .mapError(error => respond.BadRequest(ErrorResponse.single(error.toMessage)))
          .flatMap(id =>
            ProductService
              .updateProduct(id, product)
              .mapError(error => respond.NotFound(ErrorResponse.single(error.toMessage)))
          )
      )
      .as(respond.NoContent)
      .merge
}
object ProductApi {
  type Environment = ProductRepository

  val routes: ZIO[AppEnvironment, Nothing, HttpRoutes[RIO[AppEnvironment, *]]] = {
    import zio.interop.catz._
    ZIO
      .runtime[AppEnvironment]
      .map { implicit r: Runtime[AppEnvironment] =>
        new ProductsResource[RIO[AppEnvironment, *]]
      }
      .map(_.routes(new ProductApi()))
  }
}
