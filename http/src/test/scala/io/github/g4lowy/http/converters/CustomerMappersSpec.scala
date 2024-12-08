package io.github.g4lowy.http.converters

import http.generated.definitions.{CreateCustomer, UpdateCustomer}
import io.github.g4lowy.customer.domain.model._
import io.github.g4lowy.http.converters.customers._
import io.github.g4lowy.test.utils.validation.ValidationOps
import zio.Scope
import zio.test._

import java.sql.Date
import java.time.{LocalDate, LocalDateTime}

object CustomerMappersSpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment with Scope, Any] =
    suite("Customer converters should")(
      test("correctly transform CreateCustomer to Customer") {
        val createCustomer = CreateCustomer("FirstName", "LastName", Some(LocalDate.ofYearDay(2000, 315)), "+48-123456")
        val domainCustomer = createCustomer.toDomain
        assertTrue(
          domainCustomer.firstName.value.equals(createCustomer.firstName),
          domainCustomer.lastName.value.equals(createCustomer.lastName),
          domainCustomer.phone.value.equals(createCustomer.phone),
          domainCustomer.birthDate.exists(x =>
            x.toLocalDate.equals(createCustomer.birthDate.getOrElse(LocalDate.now()))
          )
        )
      },
      test("correctly transform UpdateCustomer to Customer") {
        val updateCustomer = UpdateCustomer("FirstName", "LastName", Some(LocalDate.ofYearDay(2000, 315)), "+48-123456")
        val domainCustomer = updateCustomer.toDomain
        assertTrue(
          domainCustomer.firstName.value.equals(updateCustomer.firstName),
          domainCustomer.lastName.value.equals(updateCustomer.lastName),
          domainCustomer.phone.value.equals(updateCustomer.phone),
          domainCustomer.birthDate.exists(x =>
            x.toLocalDate.equals(updateCustomer.birthDate.getOrElse(LocalDate.now()))
          )
        )
      },
      test("correctly transform Customer to GetCustomer") {
        val domainCustomer =
          Customer
            .Unvalidated(
              CustomerId.generate,
              FirstName.Unvalidated("Name"),
              LastName.Unvalidated("Surname"),
              Some(Date.valueOf(LocalDate.ofYearDay(2000, 315))),
              Phone.Unvalidated("+48-123456"),
              LocalDateTime.now()
            )
            .validate
            .asValid
        val apiCustomer = domainCustomer.toAPI
        assertTrue(
          apiCustomer.firstName.equals(domainCustomer.firstName.value),
          apiCustomer.lastName.equals(domainCustomer.lastName.value),
          apiCustomer.phone.equals(domainCustomer.phone.value),
          apiCustomer.birthDate.flatMap { apiBirthDate =>
            domainCustomer.birthDate.map(_.toLocalDate).map(domainBirthDate => apiBirthDate.isEqual(domainBirthDate))
          }.getOrElse(false)
        )
      },
      test("correctly transform domain CustomerId to api CustomerId") {
        val domainId = CustomerId.generate
        val apiId    = domainId.toAPI
        assertTrue(domainId.value.equals(apiId.value))
      }
    )
}
