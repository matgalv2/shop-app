package io.github.g4lowy.http.converters

import http.generated.definitions.{CreateAddress, CreateOrder, CreateOrderDetail, GetAddress, GetOrder, GetOrderDetail, PatchOrder}
import io.github.g4lowy.abstracttype.Id.UUIDOps
import io.github.g4lowy.http.dto.OrderDto
import io.github.g4lowy.http.dto.OrderDto.{AddressDto, OrderDetailDto, PaymentTypeDto, ShipmentTypeDto}
import io.github.g4lowy.order.domain.model._
import io.github.g4lowy.order.domain.model.address._
import io.github.g4lowy.product.domain.model.Product
import io.scalaland.chimney.dsl._

import java.time.LocalDateTime

object orders {

  // API -> DTO

  implicit class CreateOrderDetailOps(private val createOrderDetail: CreateOrderDetail) extends AnyVal {
    def toDTO: OrderDto.OrderDetailDto =
      createOrderDetail
        .transformInto[OrderDto.OrderDetailDto]
  }

  implicit class CreateAddressOps(private val createAddress: CreateAddress) extends AnyVal {
    def toDTO: OrderDto.AddressDto =
      createAddress
        .transformInto[OrderDto.AddressDto]
  }

  implicit class CreatePaymentTypeOps(private val createOrderPaymentType: CreateOrder.PaymentType) extends AnyVal {
    def toDTO: OrderDto.PaymentTypeDto =
      createOrderPaymentType match {
        case CreateOrder.PaymentType.members.BankTransfer => OrderDto.PaymentTypeDto.BankTransfer
        case CreateOrder.PaymentType.members.Card         => OrderDto.PaymentTypeDto.Card
        case CreateOrder.PaymentType.members.OnDelivery   => OrderDto.PaymentTypeDto.OnDelivery
      }
  }

  implicit class CreateShipmentTypeOps(private val createOrderShipmentType: CreateOrder.ShipmentType) extends AnyVal {
    def toDTO: OrderDto.ShipmentTypeDto =
      createOrderShipmentType match {
        case CreateOrder.ShipmentType.members.Courier => OrderDto.ShipmentTypeDto.Courier
        case CreateOrder.ShipmentType.members.Box     => OrderDto.ShipmentTypeDto.Box
        case CreateOrder.ShipmentType.members.OnPlace => OrderDto.ShipmentTypeDto.OnPlace
      }
  }

  implicit class CreateOrderOps(private val create: CreateOrder) extends AnyVal {
    def toDTO: OrderDto =
      create
        .into[OrderDto]
        .withFieldComputed(_.orderDetails, _.details.map(_.toDTO))
        .withFieldComputed(_.paymentType, _.paymentType.toDTO)
        .withFieldComputed(_.paymentAddress, _.paymentAddress.toDTO)
        .withFieldComputed(_.shipmentType, _.shipmentType.toDTO)
        .withFieldComputed(_.shipmentAddress, _.shipmentAddress.map(_.toDTO))
        .transform
  }

  // DTO -> domain

  implicit class AddressDtoOps(private val addressDto: AddressDto) extends AnyVal {
    def toDomain: Address.Unvalidated =
      addressDto
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

  implicit class PatchOrderStatusOps(private val patchStatus: PatchOrder.Status) extends AnyVal {
    def toDomain: OrderStatus =
      patchStatus match {
        case PatchOrder.Status.members.Created    => OrderStatus.Created
        case PatchOrder.Status.members.Cancelled  => OrderStatus.Cancelled
        case PatchOrder.Status.members.Paid       => OrderStatus.Paid
        case PatchOrder.Status.members.InProgress => OrderStatus.InProgress
        case PatchOrder.Status.members.Sent       => OrderStatus.Sent
        case PatchOrder.Status.members.Delivered  => OrderStatus.Delivered
        case PatchOrder.Status.members.Archived   => OrderStatus.Archived
      }
  }

  implicit class PaymentTypeDtoOps(private val paymentTypeDto: PaymentTypeDto) extends AnyVal {
    def toDomain: PaymentType =
      paymentTypeDto match {
        case PaymentTypeDto.BankTransfer => PaymentType.BankTransfer
        case PaymentTypeDto.Card         => PaymentType.Card
        case PaymentTypeDto.OnDelivery   => PaymentType.OnDelivery
      }
  }

  implicit class ShipmentTypeDtoOps(private val shipmentTypeDto: ShipmentTypeDto) extends AnyVal {
    def toDomain: ShipmentType =
      shipmentTypeDto match {
        case ShipmentTypeDto.Courier => ShipmentType.Courier
        case ShipmentTypeDto.Box     => ShipmentType.Box
        case ShipmentTypeDto.OnPlace => ShipmentType.OnPlace
      }
  }

  implicit class OrderDetailDtoOps(private val orderDetailDto: OrderDetailDto) extends AnyVal {
    def toDomain(orderId: OrderId, product: Product): OrderDetail.Unvalidated =
      orderDetailDto
        .into[OrderDetail.Unvalidated]
        .withFieldConst(_.orderId, orderId)
        .withFieldComputed(_.productId, _.productId.toId)
        .withFieldComputed(_.quantity, _.quantity)
        .withFieldConst(_.pricePerUnit, product.price.value)
        .transform

  }

  implicit class OrderDtoOps(private val orderDto: OrderDto) extends AnyVal {
    def toDomain(orderId: OrderId, details: List[OrderDetail.Unvalidated]): Order.Unvalidated =
      orderDto
        .into[Order.Unvalidated]
        .withFieldConst(_.orderId, orderId)
        .withFieldComputed(_.customerId, _.customerId.toId)
        .withFieldConst(_.details, details)
        .withFieldConst(_.orderStatus, OrderStatus.Created)
        .withFieldComputed(_.paymentType, _.paymentType.toDomain)
        .withFieldComputed(_.paymentAddress, _.paymentAddress.toDomain)
        .withFieldComputed(_.shipmentType, _.shipmentType.toDomain)
        .withFieldComputed(_.shipmentAddress, _.shipmentAddress.map(_.toDomain))
        .withFieldConst(_.createdAt, LocalDateTime.now())
        .transform
  }

  // domain -> API

  implicit class OrderDetailOps(private val orderDetail: OrderDetail) extends AnyVal {
    def toAPI: GetOrderDetail =
      orderDetail
        .into[GetOrderDetail]
        .withFieldComputed(_.productId, _.productId.value)
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
        case OrderStatus.Archived   => GetOrder.Status.Archived
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

  implicit class ShipmentTypeOps(private val shipmentType: ShipmentType) extends AnyVal {
    def toAPI: GetOrder.ShipmentType =
      shipmentType match {
        case ShipmentType.Courier => GetOrder.ShipmentType.Courier
        case ShipmentType.Box     => GetOrder.ShipmentType.Box
        case ShipmentType.OnPlace => GetOrder.ShipmentType.OnPlace
      }
  }

  implicit class OrderOps(private val order: Order) extends AnyVal {
    def toAPI: GetOrder =
      order
        .into[GetOrder]
        .withFieldComputed(_.orderId, _.orderId.value)
        .withFieldComputed(_.customerId, _.customerId.value)
        .withFieldComputed(_.status, _.orderStatus.toAPI)
        .withFieldComputed(_.details, _.details.map(_.toAPI).toVector)
        .withFieldComputed(_.totalCost, _.totalCost)
        .withFieldComputed(_.paymentType, _.paymentType.toAPI)
        .withFieldComputed(_.paymentAddress, _.paymentAddress.toAPI)
        .withFieldComputed(_.shipmentType, _.shipmentType.toAPI)
        .withFieldComputed(_.shipmentAddress, _.shipmentAddress.map(_.toAPI))
        .transform
  }

  implicit class OrderIdOps(private val orderId: OrderId) extends AnyVal {
    def toAPI: http.generated.definitions.OrderId =
      orderId
        .into[http.generated.definitions.OrderId]
        .withFieldRenamed(_.value, _.value)
        .transform
  }
}
