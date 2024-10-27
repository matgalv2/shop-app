package io.github.g4lowy.http.service

import io.github.g4lowy.customer.domain.model.{ Customer, CustomerError, CustomerId }
import io.github.g4lowy.customer.domain.repository.CustomerRepository
import zio.{ URIO, ZIO }

object CustomerService {

  def getCustomers: URIO[CustomerRepository, List[Customer]] =
    CustomerRepository.getAll

  def getCustomerById(customerId: CustomerId): ZIO[CustomerRepository, CustomerError.NotFound, Customer] =
    CustomerRepository.getById(customerId)

  def createCustomer(customer: Customer): URIO[CustomerRepository, CustomerId] = CustomerRepository.create(customer)

  def updateCustomer(
    customerId: CustomerId,
    customer: Customer
  ): ZIO[CustomerRepository, CustomerError.NotFound, Unit] =
    CustomerRepository.update(customerId, customer)

  def deleteCustomer(customerId: CustomerId): ZIO[CustomerRepository, CustomerError.NotFound, Unit] =
    /*
      TODO: after implementing orders ensure that client that is going to be deleted, doesn't have active orders
     */
    CustomerRepository.delete(customerId)
}