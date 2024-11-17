package io.github.g4lowy.http.converters

import http.generated.definitions.{ CreateAddress, CreateOrder, GetAddress, GetOrder, GetOrderDetail }
import io.github.g4lowy.customer.domain.model.Customer
import io.github.g4lowy.order.domain.model.Address._
import io.github.g4lowy.order.domain.model._
import io.scalaland.chimney.dsl._

object orders {

  implicit class OrderDetailOps(private val orderDetail: OrderDetail) extends AnyVal {
    def toAPI: GetOrderDetail =
      orderDetail
        .into[GetOrderDetail]
        .withFieldComputed(_.productId, _.productId.value)
        .transform

  }

  implicit class CreateAddressOps(private val createAddress: CreateAddress) extends AnyVal {
    def toDomain: Address.Unvalidated =
      createAddress
        .into[Address.Unvalidated]
        .withFieldConst(_.addressId, AddressId.generate)
        .withFieldComputed(_.country, x => Country.Unvalidated(x.country))
        .withFieldComputed(_.city, x => City.Unvalidated(x.city))
        .withFieldComputed(_.street, x => Street.Unvalidated(x.street))
        .withFieldComputed(_.zipCode, x => ZipCode.Unvalidated(x.zipCode))
        .withFieldComputed(_.building, x => Building.Unvalidated(x.building))
        .withFieldComputed(_.apartment, _.apartment.map(Apartment.Unvalidated))
        .transform
  }

  implicit class AddressOps(private val address: Address) extends AnyVal {
    def toAPI: GetAddress =
      address
        .into[GetAddress]
        .withFieldComputed(_.addressId, _.addressId.value)
        .withFieldComputed(_.country, _.country.value)
        .withFieldComputed(_.city, _.city.value)
        .withFieldComputed(_.street, _.street.value)
        .withFieldComputed(_.zipCode, _.zipCode.value)
        .withFieldComputed(_.building, _.building.value)
        .withFieldComputed(_.apartment, _.apartment.map(_.value))
        .transform
  }

  implicit class OrderStatusOps(private val orderStatus: OrderStatus) extends AnyVal {
    def toAPI: GetOrder.Status =
      orderStatus match {
        case OrderStatus.Created    => GetOrder.Status.Created
        case OrderStatus.Cancelled  => GetOrder.Status.Cancelled
        case OrderStatus.Paid       => GetOrder.Status.Paid
        case OrderStatus.InProgress => GetOrder.Status.InProgress
        case OrderStatus.Sent       => GetOrder.Status.Sent
        case OrderStatus.Delivered  => GetOrder.Status.Delivered
      }
  }

  implicit class PaymentTypeOps(private val paymentType: PaymentType) extends AnyVal {
    def toAPI: GetOrder.PaymentType =
      paymentType match {
        case PaymentType.BankTransfer => GetOrder.PaymentType.BankTransfer
        case PaymentType.Card         => GetOrder.PaymentType.Card
        case PaymentType.OnDelivery   => GetOrder.PaymentType.OnDelivery
      }
  }

  implicit class CreatePaymentTypeOps(private val createOrderPaymentType: CreateOrder.PaymentType) extends AnyVal {
    def toDomain: PaymentType =
      createOrderPaymentType match {
        case CreateOrder.PaymentType.members.BankTransfer => PaymentType.BankTransfer
        case CreateOrder.PaymentType.members.Card         => PaymentType.Card
        case CreateOrder.PaymentType.members.OnDelivery   => PaymentType.OnDelivery
      }
  }

  implicit class ShipmentTypeOps(private val shipmentType: ShipmentType) extends AnyVal {
    def toAPI: GetOrder.ShipmentType =
      shipmentType match {
        case ShipmentType.Courier => GetOrder.ShipmentType.Courier
        case ShipmentType.Box     => GetOrder.ShipmentType.Box
        case ShipmentType.OnPlace => GetOrder.ShipmentType.OnPlace
      }
  }

  implicit class CreateShipmentTypeOps(private val createOrderShipmentType: CreateOrder.ShipmentType) extends AnyVal {
    def toDomain: ShipmentType =
      createOrderShipmentType match {
        case CreateOrder.ShipmentType.members.Courier => ShipmentType.Courier
        case CreateOrder.ShipmentType.members.Box     => ShipmentType.Box
        case CreateOrder.ShipmentType.members.OnPlace => ShipmentType.OnPlace
      }
  }

  implicit class OrderOps(private val order: Order) extends AnyVal {
    def toAPI: GetOrder =
      order
        .into[GetOrder]
        .withFieldComputed(_.orderId, _.orderId.value)
        .withFieldComputed(_.customerId, _.customer.customerId.value)
        .withFieldComputed(_.status, _.orderStatus.toAPI)
        .withFieldComputed(_.details, _.details.map(_.toAPI).toVector)
        .withFieldComputed(_.totalCost, _.totalCost)
        .withFieldComputed(_.paymentType, _.paymentType.toAPI)
        .withFieldComputed(_.paymentAddress, _.paymentAddress.toAPI)
        .withFieldComputed(_.shipmentType, _.shipmentType.toAPI)
        .withFieldComputed(_.shipmentAddress, _.shipmentAddress.map(_.toAPI))
        .transform
  }

  implicit class CreateOrderOps(private val create: CreateOrder) extends AnyVal {
    def toDomain(customer: Customer.Unvalidated, details: List[OrderDetail.Unvalidated]): Order.Unvalidated =
      create
        .into[Order.Unvalidated]
        .withFieldConst(_.orderId, OrderId.generate)
        .withFieldConst(_.customer, customer)
        .withFieldConst(_.details, details)
        .withFieldConst(_.orderStatus, OrderStatus.Created)
        .withFieldComputed(_.paymentType, _.paymentType.toDomain)
        .withFieldComputed(_.paymentAddress, _.paymentAddress.toDomain)
        .withFieldComputed(_.shipmentType, _.shipmentType.toDomain)
        .withFieldComputed(_.shipmentAddress, _.shipmentAddress.map(_.toDomain))
        .transform
  }

}
