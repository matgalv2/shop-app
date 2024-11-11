package io.github.g4lowy.http.service

import http.generated.definitions.{ CreateCustomer, GetCustomer, UpdateCustomer }
import io.github.g4lowy.customer.domain.model.{ CustomerError, CustomerId }
import io.github.g4lowy.customer.domain.repository.CustomerRepository
import zio.{ RIO, ZIO }
import io.github.g4lowy.http.converters.customers._
import io.github.g4lowy.union.types.Union2
import io.github.g4lowy.validation.extras.ZIOValidationOps
import io.github.g4lowy.validation.validators.Validator

import java.util.UUID

object CustomerService {

  def getCustomers: RIO[CustomerRepository, Vector[GetCustomer]] =
    CustomerRepository.getAll
      .map(_.map(_.toAPI))
      .map(_.toVector)

  def getCustomerById(customerId: UUID): ZIO[CustomerRepository, CustomerError.NotFound, GetCustomer] =
    CustomerRepository.getById(CustomerId.fromUUID(customerId)).map(_.toAPI)

  def createCustomer(
    createCustomer: CreateCustomer
  ): ZIO[CustomerRepository, Validator.FailureDescription, http.generated.definitions.CustomerId] =
    ZIO
      .fromNotValidated(createCustomer.toDomain)
      .flatMap(CustomerRepository.create)
      .map(_.toAPI)

  def updateCustomer(
    customerId: UUID,
    updateCustomer: UpdateCustomer
  ): ZIO[CustomerRepository, Union2[Validator.FailureDescription, CustomerError.NotFound], Unit] =
    ZIO
      .fromNotValidated(updateCustomer.toDomain)
      .mapError(error => Union2.First(error))
      .flatMap { customer =>
        val domainId = CustomerId.fromUUID(customerId)
        CustomerRepository
          .update(domainId, customer)
          .mapError(Union2.Second.apply)
      }

  def deleteCustomer(customerId: UUID): ZIO[CustomerRepository, CustomerError.NotFound, Unit] = {
    /*
      TODO: after implementing orders ensure that client that is going to be deleted, doesn't have any orders
     */
    val domainId = CustomerId.fromUUID(customerId)
    CustomerRepository.delete(domainId)
  }
}
