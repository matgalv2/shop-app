package io.github.g4lowy.customer.domain.repository

import io.github.g4lowy.customer.domain.model.CustomerError.NotFound
import io.github.g4lowy.customer.domain.model.{Customer, CustomerError, CustomerId}
import zio.macros.accessible
import zio.{IO, UIO}

@accessible
trait CustomerRepository {
  def getById(customerId: CustomerId): IO[NotFound, Customer]
  def getAll(offset: Int, limit: Int): UIO[List[Customer]]
  def create(customer: Customer): UIO[CustomerId]
  def update(customerId: CustomerId, client: Customer): IO[CustomerError.NotFound, Unit]
  def delete(customerId: CustomerId): IO[CustomerError.NotFound, Unit]
}
