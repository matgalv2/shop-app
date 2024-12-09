package io.github.g4lowy.order.infrastructure.repository

import io.getquill.CamelCase
import io.getquill.jdbczio.Quill
import io.github.g4lowy.abstracttype.Id
import io.github.g4lowy.customer.domain.model._
import io.github.g4lowy.customer.domain.repository.CustomerRepository
import io.github.g4lowy.customer.infrastructure.repository.CustomerRepositoryPostgres
import io.github.g4lowy.order.domain.model._
import io.github.g4lowy.order.domain.model.address._
import io.github.g4lowy.order.domain.repository.OrderRepository
import io.github.g4lowy.order.infrastructure.model.{AddressSQL, OrderDetailSQL, OrderSQL}
import io.github.g4lowy.product.domain.model._
import io.github.g4lowy.product.domain.repository.ProductRepository
import io.github.g4lowy.product.infrastructure.repository.ProductRepositoryPostgres
import io.github.g4lowy.testutils.AppTestConfig
import io.github.g4lowy.testutils.TestDatabaseConfiguration.{dataSourceLive, postgresLive}
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
        test("fetch all orders") {
          val product  = makeProduct(UUID.randomUUID().toString)
          val customer = makeCustomer(UUID.randomUUID().toString)
          val orderId1 = OrderId.generate
          val orderId2 = OrderId.generate
          for {
            validatedProduct  <- ZIO.fromNotValidated(product)
            _                 <- ProductRepository.create(validatedProduct)
            validatedCustomer <- ZIO.fromNotValidated(customer)
            _                 <- CustomerRepository.create(validatedCustomer)
            orderDetail1 = OrderDetail.Unvalidated(
              orderId      = orderId1,
              productId    = product.productId,
              quantity     = 1,
              pricePerUnit = validatedProduct.price.value
            )
            orderDetail2 = OrderDetail.Unvalidated(
              orderId      = orderId2,
              productId    = product.productId,
              quantity     = 1,
              pricePerUnit = validatedProduct.price.value
            )
            order1 = makeOrder(orderId1, validatedCustomer.customerId, List(orderDetail1))
            order2 = makeOrder(orderId2, validatedCustomer.customerId, List(orderDetail2))
            validatedOrder1 <- ZIO.fromNotValidated(order1)
            validatedOrder2 <- ZIO.fromNotValidated(order2)
            beforeAdd       <- OrderRepository.getAll(0, 10)
            _               <- OrderRepository.create(validatedOrder1)
            _               <- OrderRepository.create(validatedOrder2)
            afterAdd        <- OrderRepository.getAll(0, 10)
          } yield assertTrue(beforeAdd.isEmpty, afterAdd.size == 2)
        },
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
            order = makeOrder(orderId, validatedCustomer.customerId, List(orderDetail))
            validatedOrder <- ZIO.fromNotValidated(order)
            beforeAdd      <- OrderRepository.getAll(0, 10)
            id             <- OrderRepository.create(validatedOrder)
            afterAdd       <- OrderRepository.getAll(0, 10)
          } yield assertTrue(id.value == orderId.value && beforeAdd.isEmpty && afterAdd.size == 1)
        },
        test("fetch order by id") {
          val product  = makeProduct(UUID.randomUUID().toString)
          val customer = makeCustomer(UUID.randomUUID().toString)
          val orderId  = OrderId.generate
          for {
            validatedProduct  <- ZIO.fromNotValidated(product)
            _                 <- ProductRepository.create(validatedProduct)
            validatedCustomer <- ZIO.fromNotValidated(customer)
            _                 <- CustomerRepository.create(validatedCustomer)
            orderDetail1 = OrderDetail.Unvalidated(
              orderId      = orderId,
              productId    = product.productId,
              quantity     = 1,
              pricePerUnit = validatedProduct.price.value
            )

            order = makeOrder(orderId, validatedCustomer.customerId, List(orderDetail1))
            validatedOrder1 <- ZIO.fromNotValidated(order)
            beforeAdd       <- OrderRepository.getAll(0, 10)
            _               <- OrderRepository.create(validatedOrder1)
            orderFetched    <- OrderRepository.getById(order.orderId)
          } yield assertTrue(beforeAdd.isEmpty, orderFetched.orderId == order.orderId)
        },
        test("fail when id is not found") {
          val id = OrderId.generate
          for {
            fetchedClient <- OrderRepository.getById(id).exit
          } yield assertTrue(fetchedClient.isFailure)
        },
        test("update order's status by id") {
          val product  = makeProduct(UUID.randomUUID().toString)
          val customer = makeCustomer(UUID.randomUUID().toString)
          val orderId  = OrderId.generate
          for {
            validatedProduct  <- ZIO.fromNotValidated(product)
            _                 <- ProductRepository.create(validatedProduct)
            validatedCustomer <- ZIO.fromNotValidated(customer)
            _                 <- CustomerRepository.create(validatedCustomer)
            orderDetail1 = OrderDetail.Unvalidated(
              orderId      = orderId,
              productId    = product.productId,
              quantity     = 1,
              pricePerUnit = validatedProduct.price.value
            )
            order = makeOrder(orderId, validatedCustomer.customerId, List(orderDetail1))
            validatedOrder1 <- ZIO.fromNotValidated(order)
            _               <- OrderRepository.create(validatedOrder1)
            result          <- OrderRepository.updateStatus(orderId, OrderStatus.Paid).exit
          } yield assertTrue(result.isSuccess)
        },
        test("fails when trying to downgrade order's status") {
          val product  = makeProduct(UUID.randomUUID().toString)
          val customer = makeCustomer(UUID.randomUUID().toString)
          val orderId  = OrderId.generate
          for {
            validatedProduct  <- ZIO.fromNotValidated(product)
            _                 <- ProductRepository.create(validatedProduct)
            validatedCustomer <- ZIO.fromNotValidated(customer)
            _                 <- CustomerRepository.create(validatedCustomer)
            orderDetail1 = OrderDetail.Unvalidated(
              orderId      = orderId,
              productId    = product.productId,
              quantity     = 1,
              pricePerUnit = validatedProduct.price.value
            )
            order = makeOrder(orderId, validatedCustomer.customerId, List(orderDetail1))
            validatedOrder1 <- ZIO.fromNotValidated(order)
            _               <- OrderRepository.create(validatedOrder1)
            _               <- OrderRepository.updateStatus(orderId, OrderStatus.Sent)
            result          <- OrderRepository.updateStatus(orderId, OrderStatus.Paid).exit
          } yield assertTrue(result.isFailure)
        },
        test("fail when id is not found") {
          val id = OrderId.generate
          for {
            fetchedClient <- OrderRepository.updateStatus(id, OrderStatus.Cancelled).exit
          } yield assertTrue(fetchedClient.isFailure)
        }
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
      ProductId.fromUUID(UUID.fromString(id)),
      Name.Unvalidated(name),
      Price.Unvalidated(price),
      description.map(Description.Unvalidated.apply)
    )

  private def makeCustomer(id: String) =
    Customer.Unvalidated(
      customerId = CustomerId.fromUUID(UUID.fromString(id)),
      firstName  = FirstName.Unvalidated("Rufus"),
      lastName   = LastName.Unvalidated("Goldenek"),
      birthDate  = Some(Date.valueOf("2009-03-05")),
      phone      = Phone.Unvalidated("+48-20241026"),
      createdAt  = LocalDateTime.now()
    )

  private def makeAddress(id: String) =
    Address.Unvalidated(
      addressId = AddressId.fromUUID(UUID.fromString(id)),
      country   = Country.Unvalidated("Ireland"),
      city      = City.Unvalidated("Dublin"),
      street    = Street.Unvalidated("Paderewskiego"),
      zipCode   = ZipCode.Unvalidated("12-123"),
      building  = Building.Unvalidated("12"),
      apartment = None
    )

  private def makeOrder(
    orderId: OrderId,
    customerId: Id,
    details: List[OrderDetail.Unvalidated],
    addressesId: => String = UUID.randomUUID().toString
  ) = {
    val address1 = makeAddress(addressesId)
    val address2 = makeAddress(addressesId)
    Order.Unvalidated(
      orderId         = orderId,
      customerId      = customerId,
      details         = details,
      paymentType     = PaymentType.Card,
      paymentAddress  = address1,
      shipmentType    = ShipmentType.Courier,
      shipmentAddress = Some(address2),
      orderStatus     = OrderStatus.Created
    )
  }
}
