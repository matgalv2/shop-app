package io.github.g4lowy.http.converters

import http.generated.definitions.{CreateAddress, CreateOrder, CreateOrderDetail}
import io.github.g4lowy.customer.domain.model.CustomerId
import io.github.g4lowy.http.converters.orders.{AddressOps, CreateAddressOps, CreateOrderDetailOps, CreateOrderOps, OrderDetailOps, OrderOps}
import io.github.g4lowy.order.domain.model
import io.github.g4lowy.order.domain.model.address._
import io.github.g4lowy.order.domain.model.{Order, OrderDetail, OrderId}
import io.github.g4lowy.product.domain.model.{Name, Price, Product, ProductId}
import io.github.g4lowy.testutils.validation.ValidationOps
import zio.Scope
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

import java.time.LocalDateTime
import java.util.UUID

object OrderMapperSpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment with Scope, Any] =
    suite("Order converters should")(
      test("correctly transform CreateAddressOps to Address") {
        val createAddress = CreateAddress("Country", "City", "Street", "80-808", "16e", Some("11"))
        val domainAddress = createAddress.toDomain
        assertTrue(
          domainAddress.country.value == createAddress.country,
          domainAddress.city.value == createAddress.city,
          domainAddress.street.value == createAddress.street,
          domainAddress.zipCode.value == createAddress.zipCode,
          domainAddress.building.value == createAddress.building,
          domainAddress.apartment.exists(domainApart => createAddress.apartment.contains(domainApart.value))
        )
      },
      test("correctly transform CreateOrderDetail to OrderDetail") {

        val productId         = UUID.randomUUID()
        val createOrderDetail = CreateOrderDetail(productId, 3)
        val product = Product
          .Unvalidated(
            productId   = ProductId.fromUUID(productId),
            name        = Name.Unvalidated("name"),
            price       = Price.Unvalidated(2.23),
            description = None
          )
          .validate
          .asValid

        val domainOrderDetail = createOrderDetail.toDomain(OrderId.fromUUID(UUID.randomUUID()), product)
        assertTrue(
          createOrderDetail.productId == domainOrderDetail.productId.value && createOrderDetail.quantity == domainOrderDetail.quantity
        )
      },
      test("correctly transform CreateOrder to Order") {

        val createOrderDetail = CreateOrderDetail(UUID.randomUUID(), 3)
        val product = Product
          .Unvalidated(
            productId   = ProductId.generate,
            name        = Name.Unvalidated("name"),
            price       = Price.Unvalidated(2.23),
            description = None
          )
          .validate
          .asValid

        val domainOrderDetail = createOrderDetail.toDomain(OrderId.fromUUID(UUID.randomUUID()), product)
        val createAddress     = CreateAddress("Country", "City", "Street", "80-808", "16e", Some("11"))
        val createOrder =
          CreateOrder(
            customerId      = UUID.randomUUID(),
            details         = Vector(createOrderDetail),
            paymentType     = CreateOrder.PaymentType.BankTransfer,
            paymentAddress  = createAddress,
            shipmentType    = CreateOrder.ShipmentType.Courier,
            shipmentAddress = Some(createAddress)
          )
        val domainOrder = createOrder.toDomain(OrderId.generate, List(domainOrderDetail))
        assertTrue(
          createOrder.customerId == domainOrder.customerId.value,
          createOrder.paymentType.value == domainOrder.paymentType.value,
          createOrder.shipmentType.value == domainOrder.shipmentType.value
        )
      },
      test("correctly transform domain OrderDetail to api GetOrderDetail") {
        val productId      = ProductId.generate
        val orderId        = OrderId.generate
        val orderDetail    = OrderDetail.Unvalidated(orderId, productId, 2, 2.23).validate.asValid
        val apiOrderDetail = orderDetail.toAPI

        assertTrue(
          orderDetail.productId.value == apiOrderDetail.productId,
          orderDetail.quantity == apiOrderDetail.quantity,
          orderDetail.pricePerUnit == apiOrderDetail.pricePerUnit
        )
      },
      test("correctly transform domain Address to api GetAddress") {
        val address = Address
          .Unvalidated(
            addressId = AddressId.generate,
            country   = Country.Unvalidated("Country"),
            city      = City.Unvalidated("City"),
            street    = Street.Unvalidated("Street"),
            zipCode   = ZipCode.Unvalidated("80-808"),
            building  = Building.Unvalidated("12e"),
            apartment = Some(Apartment.Unvalidated("12"))
          )
          .validate
          .asValid

        val apiAddress = address.toAPI

        assertTrue(
          address.country.value == apiAddress.country,
          address.city.value == apiAddress.city,
          address.street.value == apiAddress.street,
          address.zipCode.value == apiAddress.zipCode,
          address.building.value == apiAddress.building,
          apiAddress.apartment.exists(apiApartment =>
            address.apartment.exists(domainApartment => apiApartment == domainApartment.value)
          )
        )
      },
      test("correctly transform domain Order to api GetOrder") {
        val address = Address
          .Unvalidated(
            addressId = AddressId.generate,
            country   = Country.Unvalidated("Country"),
            city      = City.Unvalidated("City"),
            street    = Street.Unvalidated("Street"),
            zipCode   = ZipCode.Unvalidated("80-808"),
            building  = Building.Unvalidated("12e"),
            apartment = Some(Apartment.Unvalidated("12"))
          )

        val productId  = ProductId.generate
        val orderId    = OrderId.generate
        val customerId = CustomerId.generate

        val orderDetail = OrderDetail.Unvalidated(orderId, productId, 2, 2.23)

        val order = Order
          .Unvalidated(
            orderId         = orderId,
            customerId      = customerId,
            details         = List(orderDetail),
            paymentType     = model.PaymentType.BankTransfer,
            paymentAddress  = address,
            shipmentType    = model.ShipmentType.Courier,
            shipmentAddress = Some(address),
            orderStatus     = model.OrderStatus.Created,
            createdAt       = LocalDateTime.now()
          )
          .validate
          .asValid

        val apiOrder = order.toAPI

        assertTrue(
          apiOrder.orderId == order.orderId.value,
          apiOrder.customerId == order.customerId.value,
          apiOrder.paymentType.value == order.paymentType.value,
          apiOrder.shipmentType.value == order.shipmentType.value,
          apiOrder.status.value == order.orderStatus.value,
          apiOrder.createdAt == order.createdAt
        )
      }
    )
}
