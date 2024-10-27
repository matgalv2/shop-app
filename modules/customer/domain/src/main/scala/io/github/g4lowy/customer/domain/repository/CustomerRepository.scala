package io.github.g4lowy.customer.domain.repository

import io.github.g4lowy.customer.domain.model.{ Customer, CustomerError, CustomerId }
import io.github.g4lowy.customer.domain.model.CustomerError.NotFound
import zio.{ IO, UIO }
import zio.macros.accessible

@accessible
trait CustomerRepository {
  def getById(customerId: CustomerId): IO[NotFound, Customer]
  def getAll: UIO[List[Customer]]
  def create(customer: Customer): UIO[CustomerId]
  def update(customerId: CustomerId, client: Customer): IO[CustomerError.NotFound, Unit]
  def delete(customerId: CustomerId): IO[CustomerError.NotFound, Unit]
}
