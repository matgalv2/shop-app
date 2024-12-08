package io.github.g4lowy.customer.infrastructure.repository

import io.getquill.CamelCase
import io.getquill.jdbczio.Quill
import io.github.g4lowy.customer.domain.model.{Customer, CustomerError, CustomerId}
import io.github.g4lowy.customer.domain.repository.CustomerRepository
import io.github.g4lowy.customer.infrastructure.model.CustomerSQL
import zio.{IO, UIO, URLayer, ZIO, ZLayer}

case class CustomerRepositoryPostgres(quill: Quill.Postgres[CamelCase]) extends CustomerRepository {

  import quill._

  private val customers = quote {
    querySchema[CustomerSQL]("Customers")
  }

  override def create(client: Customer): UIO[CustomerId] = {
    val result =
      run {
        quote {
          customers.insertValue(lift(CustomerSQL.fromDomain(client)))
        }
      }

    result
      .as(client.customerId)
      .orDieWith(error => error)
  }

  override def getById(clientId: CustomerId): IO[CustomerError.NotFound, Customer] = {
    val result =
      run {
        quote {
          customers.filter(_.customerId == lift(clientId.value))
        }
      }
    result
      .orDieWith(error => error)
      .flatMap(_.headOption match {
        case Some(value) => ZIO.succeed(value.toDomain.validateUnsafe)
        case None        => ZIO.fail(CustomerError.NotFound(clientId))
      })
  }

  override def getAll: UIO[List[Customer]] = {
    val result = run(quote(customers))

    result
      .map(_.map(_.toDomain.validateUnsafe))
      .orDieWith(error => error)
  }

  override def update(customerId: CustomerId, customer: Customer): IO[CustomerError.NotFound, Unit] = {
    val result = run {
      quote {
        customers
          .filter(_.customerId == lift(customerId.value))
          .update(
            _.firstName -> lift(customer.firstName.value),
            _.lastName -> lift(customer.lastName.value),
            _.birthDate -> lift(customer.birthDate),
            _.phone -> lift(customer.phone.value)
          )
      }
    }

    result
      .orDieWith(error => error)
      .flatMap(rowsNo => ZIO.unless(rowsNo == 1)(ZIO.fail(CustomerError.NotFound(customerId))))
      .unit
  }

  override def delete(customerId: CustomerId): IO[CustomerError.NotFound, Unit] = {
    val result = run {
      quote {
        customers
          .filter(_.customerId == lift(customerId.value))
          .delete
      }
    }

    result
      .orDieWith(error => error)
      .flatMap(rowsNo => ZIO.fail(CustomerError.NotFound(customerId)).unless(rowsNo == 1))
      .unit
  }
}

object CustomerRepositoryPostgres {
  val live: URLayer[Quill.Postgres[CamelCase], CustomerRepositoryPostgres] =
    ZLayer.fromFunction(CustomerRepositoryPostgres.apply _)
}
