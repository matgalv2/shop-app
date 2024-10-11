package io.github.g4lowy.http.converters

import http.generated.definitions.{ CreateProduct, UpdateProduct }
import io.github.g4lowy.http.converters.products._
import io.github.g4lowy.product.domain.model.{ Description, Name, Price, Product, ProductId }
import zio.Scope
import zio.test.{ assertTrue, Spec, TestEnvironment, ZIOSpecDefault }

object ProductMappersSpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment with Scope, Any] =
    suite("Product converters should")(
      test("correctly transform CreateProduct to Product") {
        val createProduct = CreateProduct("name", 3.59, Some("description"))
        val domainProduct = createProduct.toDomain
        assertTrue(
          domainProduct.name.value.equals(createProduct.name),
          domainProduct.price.value.equals(createProduct.price),
          domainProduct.description
            .flatMap(domainDescription => createProduct.description.map(_.equals(domainDescription.value)))
            .getOrElse(false)
        )
      },
      test("correctly transform UpdateProduct to Product") {
        val updateProduct = UpdateProduct("name", 3.59, Some("description"))
        val domainProduct = updateProduct.toDomain
        assertTrue(
          domainProduct.name.value.equals(updateProduct.name),
          domainProduct.price.value.equals(updateProduct.price),
          domainProduct.description
            .flatMap(domainDescription => updateProduct.description.map(_.equals(domainDescription.value)))
            .getOrElse(false)
        )
      },
      test("correctly transform Product to GetProduct") {
        val domainProduct =
          Product
            .Unvalidated(
              ProductId.generate,
              Name.Unvalidated("Name"),
              Price.Unvalidated(3.59),
              Some(Description.Unvalidated("description"))
            )
            .unsafeValidation
        val apiProduct = domainProduct.toAPI
        assertTrue(
          apiProduct.name.equals(domainProduct.name.value),
          apiProduct.price.equals(domainProduct.price.value),
          apiProduct.description
            .flatMap(apiDescription => domainProduct.description.map(_.value.equals(apiDescription)))
            .getOrElse(false)
        )
      },
      test("correctly transform domain ProductId to api ProductId") {
        val domainId = ProductId.generate.unsafeValidation
        val apiId    = domainId.toAPI
        assertTrue(domainId.value.equals(apiId.productId))
      }
    )
}
