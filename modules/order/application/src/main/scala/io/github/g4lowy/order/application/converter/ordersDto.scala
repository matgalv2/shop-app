package io.github.g4lowy.order.application.converter

import io.github.g4lowy.abstracttype.Id.UUIDOps
import io.github.g4lowy.order.application.dto._
import io.github.g4lowy.order.domain.model._
import io.github.g4lowy.order.domain.model.address._
import io.github.g4lowy.product.domain.model.{Product, ProductId}
import io.scalaland.chimney.dsl._
object ordersDto {

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
        .withFieldComputed(_.productId, x => ProductId(x.productId))
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
        .transform
  }
}
