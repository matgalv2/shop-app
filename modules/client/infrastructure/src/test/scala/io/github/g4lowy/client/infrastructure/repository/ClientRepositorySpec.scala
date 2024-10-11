package io.github.g4lowy.client.infrastructure.repository

import io.getquill.CamelCase
import io.getquill.jdbczio.Quill
import io.getquill.mirrorContextWithQueryProbing.{ querySchema, quote }
import io.github.g4lowy.client.domain.model.{ Client, ClientId, FirstName, LastName, Phone }
import io.github.g4lowy.test.utils.AppTestConfig
import io.github.g4lowy.client.domain.repository.ClientRepository
import io.github.g4lowy.client.infrastructure.model.ClientSQL
import io.github.g4lowy.test.utils.TestDatabaseConfiguration.{ dataSource, postgresLive }
import io.github.g4lowy.validation.extras.ZIOValidationOps
import io.github.g4lowy.validation.validators.Validator.FailureDescription
import io.github.g4lowy.validation.validators.{ Validation, Validator }
import zio.test.TestAspect.sequential
import zio.{ Chunk, Scope, ZIO, ZLayer }
import zio.test._

import java.sql.Date
import java.time.{ LocalDate, LocalDateTime }
import java.util.UUID

object ClientRepositorySpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] = {
    Spec.multiple {
      Chunk(
        test("fetch all clients") {
          val clientId1 = UUID.randomUUID().toString
          val clientId2 = UUID.randomUUID().toString
          val client1   = makeClient(clientId1, "Test", "O'First", Some(Date.valueOf(LocalDate.now())), "+12-3456789")
          val client2   = makeClient(clientId2, "NotATest", "The Second", None, "+12-34567890")
          val clients   = List(client1, client2)

          val validation: Validation[FailureDescription, Iterable[Client]] =
            Validator.validateIterable[Client, Client.Unvalidated](clients)
          for {
            validated <- ZIO.fromValidation(validation)
            _         <- ZIO.foreachDiscard(validated)(ClientRepository.create)
            actual    <- ClientRepository.getAll
          } yield assertTrue(
            actual.size == 2,
            actual.map(_.clientId.value.toString).contains(clientId1),
            actual.map(_.clientId.value.toString).contains(clientId2)
          )
        },
        test("create client") {
          val client = makeClient(
            UUID.randomUUID().toString,
            "Test",
            "O'First",
            Some(Date.valueOf(LocalDate.now())),
            "+12-3456789"
          )
          for {
            beforeAdd <- ClientRepository.getAll.map(_.size)
            validated <- ZIO.fromNotValidated(client)
            _         <- ClientRepository.create(validated)
            afterAdd  <- ClientRepository.getAll
          } yield assertTrue(
            beforeAdd == 0,
            afterAdd.size == 1,
            afterAdd.head.clientId.value.toString == client.clientId.value
          )

        },
        test("fetch client by id") {
          val id     = UUID.randomUUID()
          val client = makeClient(id.toString, "Test", "O'First", Some(Date.valueOf(LocalDate.now())), "+12-3456789")
          for {
            validated     <- ZIO.fromNotValidated(client)
            _             <- ClientRepository.create(validated)
            fetchedClient <- ClientRepository.getById(validated.clientId)
          } yield assertTrue(
            fetchedClient.clientId.value == id,
            fetchedClient.firstName.value == client.firstName.value,
            fetchedClient.lastName.value == client.lastName.value,
            fetchedClient.birthDate == client.birthDate,
            fetchedClient.phone.value == client.phone.value
          )
        },
        test("fail when id is not found") {
          for {
            newId         <- ZIO.fromNotValidated(ClientId.Unvalidated(UUID.randomUUID().toString))
            fetchedClient <- ClientRepository.getById(newId).exit
          } yield assertTrue(fetchedClient.isFailure)
        },
        test("update client by id") {
          val id      = UUID.randomUUID()
          val client  = makeClient(id.toString, "Test", "O'First", Some(Date.valueOf(LocalDate.now())), "+12-3456789")
          val updated = client.copy(lastName = LastName.Unvalidated("O'Updated"))
          for {
            validated        <- ZIO.fromNotValidated(client)
            _                <- ClientRepository.create(validated)
            updateValidation <- ZIO.fromNotValidated(updated)
            _                <- ClientRepository.update(validated.clientId, updateValidation)
            fetchedClient    <- ClientRepository.getById(validated.clientId)
          } yield assertTrue(
            fetchedClient.clientId.value == id,
            fetchedClient.lastName.value == updateValidation.lastName.value
          )
        },
        test("fail when id is not found") {

          val updated = makeClient(
            UUID.randomUUID().toString,
            "Test",
            "O'Updated",
            Some(Date.valueOf(LocalDate.now())),
            "+12-3456789"
          )
          for {
            updateValidation <- ZIO.fromNotValidated(updated)
            result           <- ClientRepository.update(updateValidation.clientId, updateValidation).exit
          } yield assertTrue(result.isFailure)
        },
        test("delete client by id") {
          val id     = UUID.randomUUID()
          val client = makeClient(id.toString, "Test", "O'First", Some(Date.valueOf(LocalDate.now())), "+12-3456789")
          for {
            validated <- ZIO.fromNotValidated(client)
            _         <- ClientRepository.create(validated)
            result    <- ClientRepository.delete(validated.clientId).exit
          } yield assertTrue(result.isSuccess)
        },
        test("fail when id is not found") {
          for {
            newId  <- ZIO.fromNotValidated(ClientId.Unvalidated(UUID.randomUUID().toString))
            result <- ClientRepository.delete(newId).exit
          } yield assertTrue(result.isFailure)
        }
      )
    } @@ sequential @@ cleanTableBeforeAll @@ cleanTableAfterEach
  }.provide(
    AppTestConfig.integrationTestConfigLive,
    ZLayer.fromZIO(dataSource),
    postgresLive,
    ClientRepositoryPostgres.live
  )

  private def cleanTable =
    ZIO
      .serviceWithZIO[Quill.Postgres[CamelCase]] { quill =>
        quill.run(quote(querySchema[ClientSQL]("Clients").delete))
      }
      .unit
      .orDie

  private def cleanTableAfterEach: TestAspect[Nothing, Quill.Postgres[CamelCase], Nothing, Any] = TestAspect.after {
    cleanTable
  }

  private def cleanTableBeforeAll: TestAspect[Nothing, Quill.Postgres[CamelCase], Nothing, Any] = TestAspect.beforeAll {
    cleanTable
  }

  private def makeClient(
    id: String,
    firstName: String,
    lastName: String,
    birthDate: Option[Date],
    phone: String
  ): Client.Unvalidated =
    Client.Unvalidated(
      ClientId.Unvalidated(id),
      FirstName.Unvalidated(firstName),
      LastName.Unvalidated(lastName),
      birthDate,
      Phone.Unvalidated(phone),
      LocalDateTime.now
    )
}
