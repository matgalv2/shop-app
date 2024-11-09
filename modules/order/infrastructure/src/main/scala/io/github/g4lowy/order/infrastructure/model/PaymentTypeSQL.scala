package io.github.g4lowy.order.infrastructure.model

import io.github.g4lowy.order.domain.model.PaymentType
import io.github.g4lowy.test.utils.enums.{ EnumDecoder, EnumSQL }

sealed abstract class PaymentTypeSQL(val value: String) extends EnumSQL {
  def toDomain: PaymentType = this match {
    case PaymentTypeSQL.BankTransfer => PaymentType.BankTransfer
    case PaymentTypeSQL.Card         => PaymentType.Card
    case PaymentTypeSQL.OnDelivery   => PaymentType.OnDelivery
  }
}

object PaymentTypeSQL extends EnumDecoder[PaymentTypeSQL] {

  final case object BankTransfer extends PaymentTypeSQL("BANK_TRANSFER")
  final case object Card extends PaymentTypeSQL("CARD")
  final case object OnDelivery extends PaymentTypeSQL("ON_DELIVERY")

  override protected val values: List[PaymentTypeSQL] = List(BankTransfer, Card, OnDelivery)

  def fromDomain(paymentType: PaymentType): PaymentTypeSQL = paymentType match {
    case PaymentType.BankTransfer => PaymentTypeSQL.BankTransfer
    case PaymentType.Card         => PaymentTypeSQL.Card
    case PaymentType.OnDelivery   => PaymentTypeSQL.OnDelivery
  }
}
