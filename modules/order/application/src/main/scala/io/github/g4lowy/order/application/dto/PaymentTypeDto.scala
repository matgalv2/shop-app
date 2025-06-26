package io.github.g4lowy.order.application.dto

sealed trait PaymentTypeDto

object PaymentTypeDto {

  final case object BankTransfer extends PaymentTypeDto
  final case object Card extends PaymentTypeDto
  final case object OnDelivery extends PaymentTypeDto
}
