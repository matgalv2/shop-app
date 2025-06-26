package io.github.g4lowy.http.api

import http.generated.customers._
import http.generated.definitions.{CreateCustomer, ErrorResponse, UpdateCustomer}
import io.github.g4lowy.abstracttype.Id.UUIDOps
import io.github.g4lowy.customer.application.CustomerService
import io.github.g4lowy.customer.domain.model.CustomerId
import io.github.g4lowy.customer.domain.repository.CustomerRepository
import io.github.g4lowy.error.ErrorMessage._
import io.github.g4lowy.http.AppEnvironment
import io.github.g4lowy.http.api.CustomerApi.Environment
import io.github.g4lowy.http.converters.customers._
import io.github.g4lowy.http.error._
import io.github.g4lowy.validation.extras._
import org.http4s.HttpRoutes
import zio.{RIO, Runtime, URIO, ZIO}

import java.util.UUID

class CustomerApi extends CustomersHandler[RIO[AppEnvironment, *]] {

  override def getAllCustomers(
    respond: GetAllCustomersResponse.type
  )(offset: Option[Int], limit: Option[Int]): RIO[AppEnvironment, GetAllCustomersResponse] =
    CustomerService
      .getCustomers(offset, limit)
      .map(_.map(_.toAPI))
      .map(_.toVector)
      .map(respond.Ok)

  override def getCustomerById(
    respond: GetCustomerByIdResponse.type
  )(customerId: UUID): RIO[Environment, GetCustomerByIdResponse] =
    CustomerService
      .getCustomerById(customerId.toId)
      .mapBoth(error => respond.NotFound(ErrorResponse.single(error.toMessage)), _.toAPI)
      .map(respond.Ok)
      .merge

  override def createCustomer(
    respond: CreateCustomerResponse.type
  )(body: CreateCustomer): RIO[Environment, CreateCustomerResponse] =
    ZIO
      .fromNotValidated(body.toDomain)
      .mapError(error => respond.BadRequest(ErrorResponse.single(error.toMessage)))
      .flatMap(CustomerService.createCustomer)
      .map(customerId => respond.Created(customerId.toAPI))
      .merge

  override def updateCustomer(
    respond: UpdateCustomerResponse.type
  )(customerId: UUID, body: UpdateCustomer): RIO[Environment, UpdateCustomerResponse] =
    ZIO
      .fromNotValidated(body.toDomain)
      .mapError(error => respond.BadRequest(ErrorResponse.single(error.toMessage)))
      .flatMap(customer =>
        CustomerService
          .updateCustomer(CustomerId.fromUUID(customerId), customer)
          .mapError(error => respond.NotFound(ErrorResponse.single(error.toMessage)))
      )
      .as(respond.NoContent)
      .merge

  override def deleteCustomer(
    respond: DeleteCustomerResponse.type
  )(customerId: UUID): RIO[Environment, DeleteCustomerResponse] =
    CustomerService
      .deleteCustomer(customerId.toId)
      .mapBoth(error => respond.NotFound(ErrorResponse.single(error.toMessage)), _ => respond.NoContent)
      .merge

}
object CustomerApi {
  type Environment = CustomerRepository

  val routes: URIO[AppEnvironment, HttpRoutes[RIO[AppEnvironment, *]]] = {
    import zio.interop.catz._

    ZIO
      .runtime[AppEnvironment]
      .map { implicit r: Runtime[AppEnvironment] =>
        new CustomersResource[RIO[AppEnvironment, *]]
      }
      .map(_.routes(new CustomerApi()))
  }
}
