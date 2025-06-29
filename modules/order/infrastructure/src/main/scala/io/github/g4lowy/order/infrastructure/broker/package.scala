package io.github.g4lowy.order.infrastructure

import io.github.g4lowy.order.application.Result
import io.github.g4lowy.order.application.broker.{OrderRequestMessage, OrderResponseMessage}
import io.github.g4lowy.order.application.dto._
import zio.ZIO
import zio.json.{DecoderOps, DeriveJsonDecoder, DeriveJsonEncoder, EncoderOps, JsonDecoder, JsonEncoder, jsonDiscriminator}
import zio.kafka.serde.Serde

package object broker {

  // OrderRequestMessage

  implicit val encoderOrderRequestMessage: JsonEncoder[OrderRequestMessage] =
    DeriveJsonEncoder.gen[OrderRequestMessage]

  implicit val decoderOrderRequestMessage: JsonDecoder[OrderRequestMessage] =
    DeriveJsonDecoder.gen[OrderRequestMessage]

  implicit val serdeOrderRequestMessage: Serde[Any, OrderRequestMessage] =
    Serde.string.inmapZIO[Any, OrderRequestMessage](s =>
      ZIO
        .fromEither(s.fromJson[OrderRequestMessage])
        .mapError(e => new RuntimeException(e))
    )(r => ZIO.succeed(r.toJson))

  // OrderResponseMessage

  implicit val serdeOrderResponseMessage: Serde[Any, OrderResponseMessage] =
    Serde.string.inmapZIO[Any, OrderResponseMessage](s =>
      ZIO
        .fromEither(s.fromJson[OrderResponseMessage])
        .mapError(e => new RuntimeException(e))
    )(r => ZIO.succeed(r.toJson))

  implicit val encoderOrderResponseMessage: JsonEncoder[OrderResponseMessage] =
    DeriveJsonEncoder.gen[OrderResponseMessage]

  implicit val decoderOrderResponseMessage: JsonDecoder[OrderResponseMessage] =
    DeriveJsonDecoder.gen[OrderResponseMessage]

  // Result

  implicit val encoderResultSuccess: JsonEncoder[Result.Success] =
    DeriveJsonEncoder.gen[Result.Success]

  implicit val decoderResultSuccess: JsonDecoder[Result.Success] =
    DeriveJsonDecoder.gen[Result.Success]

  implicit val encoderResultFailure: JsonEncoder[Result.Failure] =
    DeriveJsonEncoder.gen[Result.Failure]

  implicit val decoderResultFailure: JsonDecoder[Result.Failure] =
    DeriveJsonDecoder.gen[Result.Failure]

  @jsonDiscriminator("type")
  implicit val encoderResult: JsonEncoder[Result] =
    DeriveJsonEncoder.gen[Result]

  @jsonDiscriminator("type")
  implicit val decoderResult: JsonDecoder[Result] =
    DeriveJsonDecoder.gen[Result]

  // OrderDto

  implicit val encoderOrderDto: JsonEncoder[OrderDto] =
    DeriveJsonEncoder.gen[OrderDto]

  implicit val decoderOrderDto: JsonDecoder[OrderDto] =
    DeriveJsonDecoder.gen[OrderDto]

  // OrderDetailDto

  implicit val encoderOrderDetailDto: JsonEncoder[OrderDetailDto] =
    DeriveJsonEncoder.gen[OrderDetailDto]

  implicit val decoderOrderDetailDto: JsonDecoder[OrderDetailDto] =
    DeriveJsonDecoder.gen[OrderDetailDto]

  // AddressDto

  implicit val encoderAddressDto: JsonEncoder[AddressDto] =
    DeriveJsonEncoder.gen[AddressDto]

  implicit val decoderAddressDto: JsonDecoder[AddressDto] =
    DeriveJsonDecoder.gen[AddressDto]

  // PaymentTypeDto

  implicit val encoderPaymentTypeDto: JsonEncoder[PaymentTypeDto] =
    DeriveJsonEncoder.gen[PaymentTypeDto]

  implicit val decoderPaymentTypeDto: JsonDecoder[PaymentTypeDto] =
    DeriveJsonDecoder.gen[PaymentTypeDto]

  // ShipmentTypeDto

  implicit val encoderShipmentTypeDto: JsonEncoder[ShipmentTypeDto] =
    DeriveJsonEncoder.gen[ShipmentTypeDto]

  implicit val decoderShipmentTypeDto: JsonDecoder[ShipmentTypeDto] =
    DeriveJsonDecoder.gen[ShipmentTypeDto]
}
