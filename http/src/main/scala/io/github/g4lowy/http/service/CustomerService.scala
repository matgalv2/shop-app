package io.github.g4lowy.http.service

import io.github.g4lowy.abstracttype.Id
import io.github.g4lowy.customer.domain.model.{Customer, CustomerError, CustomerId}
import io.github.g4lowy.customer.domain.repository.CustomerRepository
import zio.{URIO, ZIO}

object CustomerService {

  private val DEFAULT_OFFSET = 0
  private val DEFAULT_LIMIT  = 26

  def getCustomers(offset: Option[Int], limit: Option[Int]): URIO[CustomerRepository, List[Customer]] =
    CustomerRepository.getAll(offset.getOrElse(DEFAULT_OFFSET), limit.getOrElse(DEFAULT_LIMIT))

  def getCustomerById(id: Id): ZIO[CustomerRepository, CustomerError.NotFound, Customer] =
    CustomerRepository.getById(CustomerId.fromUUID(id.value))

  def createCustomer(customer: Customer): URIO[CustomerRepository, CustomerId] = CustomerRepository.create(customer)

  def updateCustomer(
    customerId: CustomerId,
    customer: Customer
  ): ZIO[CustomerRepository, CustomerError.NotFound, Unit] =
    CustomerRepository.update(customerId, customer)

  def deleteCustomer(id: Id): ZIO[CustomerRepository, CustomerError.NotFound, Unit] =
    /*
      TODO: after implementing orders ensure that client that is going to be deleted, doesn't have active orders
     */
    CustomerRepository.delete(CustomerId.fromUUID(id.value))
}
