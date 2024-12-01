package io.github.g4lowy.order.infrastructure.repository

import io.getquill.CamelCase
import io.getquill.jdbczio.Quill
import io.github.g4lowy.customer.domain.model._
import io.github.g4lowy.customer.domain.repository.CustomerRepository
import io.github.g4lowy.customer.infrastructure.repository.CustomerRepositoryPostgres
import io.github.g4lowy.order.domain.model.Address._
import io.github.g4lowy.order.domain.model._
import io.github.g4lowy.order.domain.repository.OrderRepository
import io.github.g4lowy.order.infrastructure.model.{AddressSQL, OrderDetailSQL, OrderSQL}
import io.github.g4lowy.product.domain.model._
import io.github.g4lowy.product.domain.repository.ProductRepository
import io.github.g4lowy.product.infrastructure.repository.ProductRepositoryPostgres
import io.github.g4lowy.test.utils.AppTestConfig
import io.github.g4lowy.test.utils.TestDatabaseConfiguration.{dataSourceLive, postgresLive}
import io.github.g4lowy.validation.extras.ZIOValidationOps
import zio.test.TestAspect.sequential
import zio.test._
import zio.{Chunk, Scope, ZIO}

import java.sql.Date
import java.time.LocalDateTime
import java.util.UUID

object OrderRepositorySpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] = {
    Spec.multiple {
      Chunk(
//        test("fetch all products") {
//          val productId1 = UUID.randomUUID().toString
//          val productId2 = UUID.randomUUID().toString
//          val product1   = makeProduct(productId1, "Product1 ", 3.59, Some("some description"))
//          val product2   = makeProduct(productId2, "Product2", 17.33, None)
//          val products   = List(product1, product2)
//
//          val validation: Validation[FailureDescription, Iterable[Product]] =
//            Validator.validateIterable[Product, Product.Unvalidated](products)
//          for {
//            validated <- ZIO.fromValidation(validation)
//            _         <- ZIO.foreachDiscard(validated)(ProductRepository.create)
//            actual    <- ProductRepository.getAll
//          } yield assertTrue(
//            actual.size == 2,
//            actual.map(_.productId.value.toString).contains(productId1),
//            actual.map(_.productId.value.toString).contains(productId2)
//          )
//        },
        test("create order") {
          val product  = makeProduct(UUID.randomUUID().toString)
          val customer = makeCustomer(UUID.randomUUID().toString)
          val orderId  = OrderId.generate
          for {
            validatedProduct  <- ZIO.fromNotValidated(product)
            _                 <- ProductRepository.create(validatedProduct)
            validatedCustomer <- ZIO.fromNotValidated(customer)
            _                 <- CustomerRepository.create(validatedCustomer)
            orderDetail = OrderDetail.Unvalidated(
              orderId      = orderId,
              productId    = product.productId,
              quantity     = 1,
              pricePerUnit = validatedProduct.price.value
            )

            order = makeOrder(orderId, validatedCustomer, List(orderDetail))
            validatedOrder <- ZIO.fromNotValidated(order)
            beforeAdd      <- OrderRepository.getAll(0, 10)
            id             <- OrderRepository.create(validatedOrder)
            afterAdd       <- OrderRepository.getAll(0, 10)
          } yield assertTrue(id.value.toString == orderId.value && beforeAdd.isEmpty && afterAdd.size == 1)
        }
//        test("fetch product by id") {
//          val id      = UUID.randomUUID()
//          val product = makeProduct(id.toString)
//          for {
//            validated     <- ZIO.fromNotValidated(product)
//            _             <- ProductRepository.create(validated)
//            fetchedClient <- ProductRepository.getById(validated.productId)
//          } yield assertTrue(
//            fetchedClient.productId.value == id,
//            fetchedClient.name.value == product.name.value,
//            fetchedClient.price.value == product.price.value,
//            fetchedClient.description.map(_.value) == product.description.map(_.value)
//          )
//        },
//        test("fail when id is not found") {
//          for {
//            newId         <- ZIO.fromNotValidated(ProductId.Unvalidated(UUID.randomUUID().toString))
//            fetchedClient <- ProductRepository.getById(newId).exit
//          } yield assertTrue(fetchedClient.isFailure)
//        },
//        test("update product by id") {
//          val id      = UUID.randomUUID()
//          val client  = makeProduct(id.toString)
//          val updated = client.copy(name = Name.Unvalidated("O'Updated"))
//          for {
//            validated        <- ZIO.fromNotValidated(client)
//            _                <- ProductRepository.create(validated)
//            updateValidation <- ZIO.fromNotValidated(updated)
//            _                <- ProductRepository.update(validated.productId, updateValidation)
//            fetchedClient    <- ProductRepository.getById(validated.productId)
//          } yield assertTrue(
//            fetchedClient.productId.value == id,
//            fetchedClient.name.value == updateValidation.name.value
//          )
//        },
//        test("fail when id is not found") {
//
//          val updated = makeProduct(UUID.randomUUID().toString)
//          for {
//            updateValidation <- ZIO.fromNotValidated(updated)
//            result           <- ProductRepository.update(updateValidation.productId, updateValidation).exit
//          } yield assertTrue(result.isFailure)
//        },
//        test("delete product by id") {
//          val id     = UUID.randomUUID()
//          val client = makeProduct(id.toString)
//          for {
//            validated <- ZIO.fromNotValidated(client)
//            _         <- ProductRepository.create(validated)
//            result    <- ProductRepository.delete(validated.productId).exit
//          } yield assertTrue(result.isSuccess)
//        },
//        test("fail when id is not found") {
//          for {
//            newId  <- ZIO.fromNotValidated(ProductId.Unvalidated(UUID.randomUUID().toString))
//            result <- ProductRepository.delete(newId).exit
//          } yield assertTrue(result.isFailure)
//        }
      )
    } @@ sequential @@ cleanTableBeforeAll @@ cleanTableAfterEach
  }.provide(
    AppTestConfig.integrationTestConfigLive,
    dataSourceLive,
    postgresLive,
    ProductRepositoryPostgres.live,
    OrderRepositoryPostgres.live,
    CustomerRepositoryPostgres.live
  )

  private def cleanTable =
    ZIO
      .serviceWithZIO[Quill.Postgres[CamelCase]] { quill =>
        import quill._
        for {
          _ <- quill.run(quote(querySchema[OrderDetailSQL]("OrdersDetails").delete))
          _ <- quill.run(quote(querySchema[OrderSQL]("Orders").delete))
          _ <- quill.run(quote(querySchema[AddressSQL]("Addresses").delete))
        } yield ()
      }
      .unit
      .orDie

  private def cleanTableAfterEach = TestAspect.after(cleanTable)

  private def cleanTableBeforeAll = TestAspect.beforeAll(cleanTable)

  private def makeProduct(
    id: String,
    name: String                = "Product",
    price: Double               = 3.59,
    description: Option[String] = None
  ): Product.Unvalidated =
    Product.Unvalidated(
      ProductId.Unvalidated(id),
      Name.Unvalidated(name),
      Price.Unvalidated(price),
      description.map(Description.Unvalidated.apply)
    )

  private def makeCustomer(id: String) =
    Customer.Unvalidated(
      customerId = CustomerId.Unvalidated(id),
      firstName  = FirstName.Unvalidated("Rufus"),
      lastName   = LastName.Unvalidated("Goldenek"),
      birthDate  = Some(Date.valueOf("2009-03-05")),
      phone      = Phone.Unvalidated("+48-20241026"),
      createdAt  = LocalDateTime.now()
    )
  private def makeAddress(id: String) =
    Address.Unvalidated(
      addressId = Address.AddressId.Unvalidated(id),
      country   = Country.Unvalidated("Ireland"),
      city      = City.Unvalidated("Dublin"),
      street    = Street.Unvalidated("Paderewskiego"),
      zipCode   = ZipCode.Unvalidated("12-123"),
      building  = Building.Unvalidated("12"),
      apartment = None
    )

  private def makeOrder(
    orderId: OrderId.Unvalidated,
    customer: Customer,
    details: List[OrderDetail.Unvalidated],
    addressesId: => String = UUID.randomUUID().toString
  ) = {
    val address1 = makeAddress(addressesId)
    val address2 = makeAddress(addressesId)
    Order.Unvalidated(
      orderId         = orderId,
      customer        = customer,
      details         = details,
      paymentType     = PaymentType.Card,
      paymentAddress  = address1,
      shipmentType    = ShipmentType.Courier,
      shipmentAddress = Some(address2),
      orderStatus     = OrderStatus.Created
    )
  }
}
