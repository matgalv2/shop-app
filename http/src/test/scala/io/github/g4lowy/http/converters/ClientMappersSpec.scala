package io.github.g4lowy.http.converters

import http.generated.definitions.{CreateClient, UpdateClient}
import io.github.g4lowy.client.domain.model.{Client, ClientId, FirstName, LastName, Phone}
import io.github.g4lowy.http.converters.clients._
import zio.Scope
import zio.test._

import java.sql.Date
import java.time.{LocalDate, LocalDateTime}

object ClientMappersSpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment with Scope, Any] =
    suite("Client converters should")(
      test("correctly transform CreateClient to Client") {
        val createClient = CreateClient("FirstName", "LastName", Some(LocalDate.ofYearDay(2000, 315)), "+48-123456")
        val domainClient = createClient.toDomain
        assertTrue(
          domainClient.firstName.value.equals(createClient.firstName),
          domainClient.lastName.value.equals(createClient.lastName),
          domainClient.phone.value.equals(createClient.phone),
          domainClient.birthDate.exists(x => x.toLocalDate.equals(createClient.birthDate.getOrElse(LocalDate.now())))
        )
      },
      test("correctly transform UpdateClient to Client") {
        val updateClient = UpdateClient("FirstName", "LastName", Some(LocalDate.ofYearDay(2000, 315)), "+48-123456")
        val domainClient = updateClient.toDomain
        assertTrue(
          domainClient.firstName.value.equals(updateClient.firstName),
          domainClient.lastName.value.equals(updateClient.lastName),
          domainClient.phone.value.equals(updateClient.phone),
          domainClient.birthDate.exists(x => x.toLocalDate.equals(updateClient.birthDate.getOrElse(LocalDate.now())))
        )
      },
      test("correctly transform Client to GetClient") {
        val domainClient =
          Client
            .Unvalidated(
              ClientId.generate,
              FirstName.Unvalidated("Name"),
              LastName.Unvalidated("Surname"),
              Some(Date.valueOf(LocalDate.ofYearDay(2000, 315))),
              Phone.Unvalidated("+48-123456"),
              LocalDateTime.now()
            )
            .unsafeValidation
        val apiClient = domainClient.toAPI
        assertTrue(
          apiClient.firstName.equals(domainClient.firstName.value),
          apiClient.lastName.equals(domainClient.lastName.value),
          apiClient.phone.equals(domainClient.phone.value),
          apiClient.birthDate.flatMap { apiBirthDate =>
            domainClient.birthDate.map(_.toLocalDate).map(domainBirthDate => apiBirthDate.isEqual(domainBirthDate))
          }.getOrElse(false)
        )
      },
      test("correctly transform domain ClientId to api ClientId") {
        val domainId = ClientId.generate.unsafeValidation
        val apiId    = domainId.toAPI
        assertTrue(domainId.value.equals(apiId.clientId))
      }
    )
}
