package io.github.g4lowy.product.infrastructure.repository

import io.getquill.CamelCase
import io.getquill.jdbczio.Quill
import io.getquill.mirrorContextWithQueryProbing.{querySchema, quote}
import io.github.g4lowy.product.domain.model._
import io.github.g4lowy.product.domain.repository.ProductRepository
import io.github.g4lowy.product.infrastructure.model.ProductSQL
import io.github.g4lowy.testutils.AppTestConfig
import io.github.g4lowy.testutils.TestDatabaseConfiguration.{dataSourceLive, postgresLive}
import io.github.g4lowy.validation.extras.ZIOValidationOps
import io.github.g4lowy.validation.validators.Validator.FailureDescription
import io.github.g4lowy.validation.validators.{Validation, Validator}
import zio.test.TestAspect.sequential
import zio.test._
import zio.{Chunk, Scope, ZIO}

import java.util.UUID

object ProductRepositorySpec extends ZIOSpecDefault {

  private val DEFAULT_OFFSET = 0
  private val DEFAULT_LIMIT  = 10

  override def spec: Spec[TestEnvironment with Scope, Any] = {
    Spec.multiple {
      Chunk(
        test("fetch all products") {
          val productId1 = UUID.randomUUID().toString
          val productId2 = UUID.randomUUID().toString
          val product1   = makeProduct(productId1, "Product1 ", 3.59, Some("some description"))
          val product2   = makeProduct(productId2, "Product2", 17.33, None)
          val products   = List(product1, product2)

          val validation: Validation[FailureDescription, Iterable[Product]] =
            Validator.validateIterable[Product, Product.Unvalidated](products)
          for {
            validated <- ZIO.fromValidation(validation)
            _         <- ZIO.foreachDiscard(validated)(ProductRepository.create)
            actual    <- ProductRepository.getAll(DEFAULT_OFFSET, DEFAULT_LIMIT)
          } yield assertTrue(
            actual.size == 2,
            actual.map(_.productId.value.toString).contains(productId1),
            actual.map(_.productId.value.toString).contains(productId2)
          )
        },
        test("create product") {
          val product = makeProduct(UUID.randomUUID().toString)
          for {
            beforeAdd <- ProductRepository.getAll(DEFAULT_OFFSET, DEFAULT_LIMIT).map(_.size)
            validated <- ZIO.fromNotValidated(product)
            _         <- ProductRepository.create(validated)
            afterAdd  <- ProductRepository.getAll(DEFAULT_OFFSET, DEFAULT_LIMIT)
          } yield assertTrue(
            beforeAdd == 0,
            afterAdd.size == 1,
            afterAdd.head.productId.value == product.productId.value
          )

        },
        test("fetch product by id") {
          val id      = UUID.randomUUID()
          val product = makeProduct(id.toString)
          for {
            validated     <- ZIO.fromNotValidated(product)
            _             <- ProductRepository.create(validated)
            fetchedClient <- ProductRepository.getById(validated.productId)
          } yield assertTrue(
            fetchedClient.productId.value == id,
            fetchedClient.name.value == product.name.value,
            fetchedClient.price.value == product.price.value,
            fetchedClient.description.map(_.value) == product.description.map(_.value)
          )
        },
        test("fail when id is not found") {
          for {
            newId         <- ZIO.succeed(ProductId.generate)
            fetchedClient <- ProductRepository.getById(newId).exit
          } yield assertTrue(fetchedClient.isFailure)
        },
        test("update product by id") {
          val id      = UUID.randomUUID()
          val client  = makeProduct(id.toString)
          val updated = client.copy(name = Name.Unvalidated("O'Updated"))
          for {
            validated        <- ZIO.fromNotValidated(client)
            _                <- ProductRepository.create(validated)
            updateValidation <- ZIO.fromNotValidated(updated)
            _                <- ProductRepository.update(validated.productId, updateValidation)
            fetchedClient    <- ProductRepository.getById(validated.productId)
          } yield assertTrue(
            fetchedClient.productId.value == id,
            fetchedClient.name.value == updateValidation.name.value
          )
        },
        test("fail when id is not found") {

          val updated = makeProduct(UUID.randomUUID().toString)
          for {
            updateValidation <- ZIO.fromNotValidated(updated)
            result           <- ProductRepository.update(updateValidation.productId, updateValidation).exit
          } yield assertTrue(result.isFailure)
        },
        test("delete product by id") {
          val id     = UUID.randomUUID()
          val client = makeProduct(id.toString)
          for {
            validated <- ZIO.fromNotValidated(client)
            _         <- ProductRepository.create(validated)
            result    <- ProductRepository.delete(validated.productId).exit
          } yield assertTrue(result.isSuccess)
        },
        test("fail when id is not found") {
          for {
            newId  <- ZIO.succeed(ProductId.generate)
            result <- ProductRepository.delete(newId).exit
          } yield assertTrue(result.isFailure)
        }
      )
    } @@ sequential @@ cleanTableBeforeAll @@ cleanTableAfterEach
  }.provide(AppTestConfig.integrationTestConfigLive, dataSourceLive, postgresLive, ProductRepositoryPostgres.live)

  private def cleanTable =
    ZIO
      .serviceWithZIO[Quill.Postgres[CamelCase]] { quill =>
        quill.run(quote(querySchema[ProductSQL]("products").delete))
      }
      .unit
      .orDie

  private def cleanTableAfterEach = TestAspect.after(cleanTable)

  private def cleanTableBeforeAll = TestAspect.beforeAll(cleanTable)

  private def makeProduct(
    id: String,
    name: String                = "Product",
    price: Double               = 3.59,
    description: Option[String] = None
  ): Product.Unvalidated =
    Product.Unvalidated(
      ProductId(UUID.fromString(id)),
      Name.Unvalidated(name),
      Price.Unvalidated(price),
      description.map(Description.Unvalidated.apply)
    )
}
