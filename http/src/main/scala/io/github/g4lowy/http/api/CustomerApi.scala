package io.github.g4lowy.http.api

import http.generated.customers.{
  CreateCustomerResponse,
  CustomersHandler,
  CustomersResource,
  DeleteCustomerResponse,
  GetAllCustomersResponse,
  GetCustomerByIdResponse,
  UpdateCustomerResponse
}
import http.generated.definitions.{ CreateCustomer, ErrorResponse, UpdateCustomer }
import io.github.g4lowy.customer.domain.model.CustomerError
import io.github.g4lowy.customer.domain.repository.CustomerRepository
import io.github.g4lowy.http.api.CustomerApi.Environment
import io.github.g4lowy.http.service.CustomerService
import zio.{ RIO, Runtime, ZIO }
import io.github.g4lowy.http.error._
import io.github.g4lowy.error.ErrorMessage._
import io.github.g4lowy.http.AppEnvironment
import io.github.g4lowy.union.types.Union2._
import io.github.g4lowy.validation.validators.Validator.FailureDescription
import org.http4s.HttpRoutes

import java.util.UUID

class CustomerApi extends CustomersHandler[RIO[AppEnvironment, *]] {

  override def getAllCustomers(respond: GetAllCustomersResponse.type)(): RIO[Environment, GetAllCustomersResponse] =
    CustomerService.getCustomers
      .map(respond.Ok)

  override def getCustomerById(
    respond: GetCustomerByIdResponse.type
  )(customerId: UUID): RIO[Environment, GetCustomerByIdResponse] =
    CustomerService
      .getCustomerById(customerId)
      .mapBoth(error => respond.NotFound(ErrorResponse.single(error.toMessage)), respond.Ok)
      .merge

  override def createCustomer(
    respond: CreateCustomerResponse.type
  )(body: CreateCustomer): RIO[Environment, CreateCustomerResponse] =
    CustomerService
      .createCustomer(body)
      .mapBoth(error => respond.BadRequest(ErrorResponse.single(error.toMessage)), respond.Created.apply)
      .merge

  override def updateCustomer(
    respond: UpdateCustomerResponse.type
  )(customerId: UUID, body: UpdateCustomer): RIO[Environment, UpdateCustomerResponse] =
    CustomerService
      .updateCustomer(customerId, body)
      .mapError {
        case First(failureDescription: FailureDescription) =>
          respond.BadRequest(ErrorResponse.single(failureDescription.toMessage))

        case Second(notFoundError: CustomerError.NotFound) =>
          respond.NotFound(ErrorResponse.single(notFoundError.toMessage))
      }
      .as(respond.NoContent)
      .merge

  override def deleteCustomer(
    respond: DeleteCustomerResponse.type
  )(customerId: UUID): RIO[Environment, DeleteCustomerResponse] =
    CustomerService
      .deleteCustomer(customerId)
      .mapBoth(error => respond.NotFound(ErrorResponse.single(error.toMessage)), _ => respond.NoContent)
      .merge

}
object CustomerApi {
  type Environment = CustomerRepository

  val routes: ZIO[AppEnvironment, Nothing, HttpRoutes[RIO[AppEnvironment, *]]] = {
    import zio.interop.catz._

    ZIO
      .runtime[AppEnvironment]
      .map { implicit r: Runtime[AppEnvironment] =>
        new CustomersResource[RIO[AppEnvironment, *]]
      }
      .map(_.routes(new CustomerApi()))
  }
}
