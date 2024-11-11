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
import io.github.g4lowy.product.domain.model.{ ProductError, ProductId }
import io.github.g4lowy.validation.extras.ZIOValidationOps
import io.github.g4lowy.validation.validators.Validator
import org.http4s.HttpRoutes

import java.util.UUID

class ProductApi extends ProductsHandler[RIO[AppEnvironment, *]] {

  override def createProduct(
    respond: CreateProductResponse.type
  )(body: CreateProduct): RIO[Environment, CreateProductResponse] =
    ProductService
      .createProduct(body)
      .mapBoth(
        error => respond.BadRequest(ErrorResponse.single(error.toMessage)),
        productId => respond.Created(productId.toAPI)
      )
      .merge

  override def getAllProducts(respond: GetAllProductsResponse.type)(): RIO[Environment, GetAllProductsResponse] =
    ProductService.getProducts.map(respond.Ok)

  override def getProductById(
    respond: GetProductByIdResponse.type
  )(productId: UUID): RIO[Environment, GetProductByIdResponse] =
    ProductService
      .getProductById(productId)
      .mapBoth(error => respond.NotFound(ErrorResponse.single(error.toMessage)), _.toAPI)
      .map(respond.Ok)
      .merge

  override def deleteProduct(
    respond: DeleteProductResponse.type
  )(productId: UUID): RIO[Environment, DeleteProductResponse] =
    ProductService
      .deleteProduct(productId)
      .mapBoth(error => respond.NotFound(ErrorResponse.single(error.toMessage)), _ => respond.NoContent)
      .merge

  override def updateProduct(
    respond: UpdateProductResponse.type
  )(productId: UUID, body: UpdateProduct): RIO[Environment, UpdateProductResponse] =
    ProductService
      .updateProduct(productId, body)
      .mapError(_.map {
        case error: Validator.FailureDescription =>
          respond.BadRequest(ErrorResponse.single(error.toMessage))
        case error: ProductError.NotFound => respond.NotFound(ErrorResponse.single(error.toMessage))
      })
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
