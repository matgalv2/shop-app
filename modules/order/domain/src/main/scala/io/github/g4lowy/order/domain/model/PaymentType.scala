package io.github.g4lowy.order.domain.model

sealed abstract class PaymentType(val value: String)

object PaymentType {
  final case object BankTransfer extends PaymentType("BANK_TRANSFER")
  final case object Card extends PaymentType("CARD")
  final case object OnDelivery extends PaymentType("ON_DELIVERY")

}
