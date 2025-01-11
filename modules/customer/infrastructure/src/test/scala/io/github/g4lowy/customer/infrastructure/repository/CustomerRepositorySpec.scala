package io.github.g4lowy.customer.infrastructure.repository

import io.getquill.CamelCase
import io.getquill.jdbczio.Quill
import io.getquill.mirrorContextWithQueryProbing.{querySchema, quote}
import io.github.g4lowy.customer.domain.model._
import io.github.g4lowy.customer.domain.repository.CustomerRepository
import io.github.g4lowy.customer.infrastructure.model.CustomerSQL
import io.github.g4lowy.testutils.AppTestConfig
import io.github.g4lowy.testutils.TestDatabaseConfiguration.{dataSource, postgresLive}
import io.github.g4lowy.validation.extras.ZIOValidationOps
import io.github.g4lowy.validation.validators.Validator.FailureDescription
import io.github.g4lowy.validation.validators.{Validation, Validator}
import zio.test.TestAspect.sequential
import zio.test._
import zio.{Chunk, Scope, ZIO, ZLayer}

import java.sql.Date
import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

object CustomerRepositorySpec extends ZIOSpecDefault {

  private val DEFAULT_OFFSET = 0
  private val DEFAULT_LIMIT  = 10

  override def spec: Spec[TestEnvironment with Scope, Any] = {
    Spec.multiple {
      Chunk(
        test("fetch all customers") {
          val customerId1 = UUID.randomUUID().toString
          val customerId2 = UUID.randomUUID().toString
          val customer1 =
            makeCustomer(customerId1, "Test", "O'First", Some(Date.valueOf(LocalDate.now())), "+12-3456789")
          val customer2 = makeCustomer(customerId2, "NotATest", "The Second", None, "+12-34567890")
          val customers = List(customer1, customer2)

          val validation: Validation[FailureDescription, Iterable[Customer]] =
            Validator.validateIterable[Customer, Customer.Unvalidated](customers)
          for {
            validated <- ZIO.fromValidation(validation)
            _         <- ZIO.foreachDiscard(validated)(CustomerRepository.create)
            actual    <- CustomerRepository.getAll(DEFAULT_OFFSET, DEFAULT_LIMIT)
          } yield assertTrue(
            actual.size == 2,
            actual.map(_.customerId.value.toString).contains(customerId1),
            actual.map(_.customerId.value.toString).contains(customerId2)
          )
        },
        test("create customer") {
          val customer = makeCustomer(
            UUID.randomUUID().toString,
            "Test",
            "O'First",
            Some(Date.valueOf(LocalDate.now())),
            "+12-3456789"
          )
          for {
            beforeAdd <- CustomerRepository.getAll(DEFAULT_OFFSET, DEFAULT_LIMIT).map(_.size)
            validated <- ZIO.fromNotValidated(customer)
            _         <- CustomerRepository.create(validated)
            afterAdd  <- CustomerRepository.getAll(DEFAULT_OFFSET, DEFAULT_LIMIT)
          } yield assertTrue(
            beforeAdd == 0,
            afterAdd.size == 1,
            afterAdd.head.customerId.value == customer.customerId.value
          )

        },
        test("fetch customer by id") {
          val id = UUID.randomUUID()
          val customer =
            makeCustomer(id.toString, "Test", "O'First", Some(Date.valueOf(LocalDate.now())), "+12-3456789")
          for {
            validated       <- ZIO.fromNotValidated(customer)
            _               <- CustomerRepository.create(validated)
            fetchedCustomer <- CustomerRepository.getById(validated.customerId)
          } yield assertTrue(
            fetchedCustomer.customerId.value == id,
            fetchedCustomer.firstName.value == customer.firstName.value,
            fetchedCustomer.lastName.value == customer.lastName.value,
            fetchedCustomer.birthDate == customer.birthDate,
            fetchedCustomer.phone.value == customer.phone.value
          )
        },
        test("fail when id is not found") {
          for {
            newId           <- ZIO.succeed(CustomerId.generate)
            fetchedCustomer <- CustomerRepository.getById(newId).exit
          } yield assertTrue(fetchedCustomer.isFailure)
        },
        test("update customer by id") {
          val id = UUID.randomUUID()
          val customer =
            makeCustomer(id.toString, "Test", "O'First", Some(Date.valueOf(LocalDate.now())), "+12-3456789")
          val updated = customer.copy(lastName = LastName.Unvalidated("O'Updated"))
          for {
            validated        <- ZIO.fromNotValidated(customer)
            _                <- CustomerRepository.create(validated)
            updateValidation <- ZIO.fromNotValidated(updated)
            _                <- CustomerRepository.update(validated.customerId, updateValidation)
            fetchedCustomer  <- CustomerRepository.getById(validated.customerId)
          } yield assertTrue(
            fetchedCustomer.customerId.value == id,
            fetchedCustomer.lastName.value == updateValidation.lastName.value
          )
        },
        test("fail when id is not found") {

          val updated = makeCustomer(
            UUID.randomUUID().toString,
            "Test",
            "O'Updated",
            Some(Date.valueOf(LocalDate.now())),
            "+12-3456789"
          )
          for {
            updateValidation <- ZIO.fromNotValidated(updated)
            result           <- CustomerRepository.update(updateValidation.customerId, updateValidation).exit
          } yield assertTrue(result.isFailure)
        },
        test("delete customer by id") {
          val id = UUID.randomUUID()
          val customer =
            makeCustomer(id.toString, "Test", "O'First", Some(Date.valueOf(LocalDate.now())), "+12-3456789")
          for {
            validated <- ZIO.fromNotValidated(customer)
            _         <- CustomerRepository.create(validated)
            result    <- CustomerRepository.delete(validated.customerId).exit
          } yield assertTrue(result.isSuccess)
        },
        test("fail when id is not found") {
          for {
            newId  <- ZIO.succeed(CustomerId.generate)
            result <- CustomerRepository.delete(newId).exit
          } yield assertTrue(result.isFailure)
        }
      )
    } @@ sequential @@ cleanTableBeforeAll @@ cleanTableAfterEach
  }.provide(
    AppTestConfig.integrationTestConfigLive,
    ZLayer.fromZIO(dataSource),
    postgresLive,
    CustomerRepositoryPostgres.live
  )

  private def cleanTable =
    ZIO
      .serviceWithZIO[Quill.Postgres[CamelCase]] { quill =>
        quill.run(quote(querySchema[CustomerSQL]("customers").delete))
      }
      .unit
      .orDie

  private def cleanTableAfterEach: TestAspect[Nothing, Quill.Postgres[CamelCase], Nothing, Any] = TestAspect.after {
    cleanTable
  }

  private def cleanTableBeforeAll: TestAspect[Nothing, Quill.Postgres[CamelCase], Nothing, Any] = TestAspect.beforeAll {
    cleanTable
  }

  private def makeCustomer(
    id: String,
    firstName: String,
    lastName: String,
    birthDate: Option[Date],
    phone: String
  ): Customer.Unvalidated =
    Customer.Unvalidated(
      CustomerId(UUID.fromString(id)),
      FirstName.Unvalidated(firstName),
      LastName.Unvalidated(lastName),
      birthDate,
      Phone.Unvalidated(phone),
      LocalDateTime.now
    )
}
