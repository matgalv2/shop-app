package io.github.g4lowy.order.infrastructure.broker

import io.github.g4lowy.order.application.broker.{OrderRequestMessage, OrderResponseMessage, Result}
import io.github.g4lowy.order.application.dto._
import zio.ZIO
import zio.json._
import zio.kafka.serde.Serde

object OrderCodecs {

  private def deriveSerde[A: JsonEncoder: JsonDecoder]: Serde[Any, A] =
    Serde.string.inmapZIO[Any, A](json =>
      ZIO
        .fromEither(json.fromJson[A])
        .mapError(err => new RuntimeException(err))
    )(msg => ZIO.succeed(msg.toJson))

  // Result

  implicit val encoderResultSuccess: JsonEncoder[Result.Success] = DeriveJsonEncoder.gen[Result.Success]

  implicit val decoderResultSuccess: JsonDecoder[Result.Success] = DeriveJsonDecoder.gen[Result.Success]

  implicit val encoderResultFailure: JsonEncoder[Result.Failure] = DeriveJsonEncoder.gen[Result.Failure]

  implicit val decoderResultFailure: JsonDecoder[Result.Failure] = DeriveJsonDecoder.gen[Result.Failure]

  @jsonDiscriminator("type")
  implicit val encoderResult: JsonEncoder[Result] = DeriveJsonEncoder.gen[Result]

  @jsonDiscriminator("type")
  implicit val decoderResult: JsonDecoder[Result] = DeriveJsonDecoder.gen[Result]

  implicit val addressDtoCodec: JsonCodec[AddressDto] = DeriveJsonCodec.gen[AddressDto]

  implicit val paymentTypeDtoCodec: JsonCodec[PaymentTypeDto] = DeriveJsonCodec.gen[PaymentTypeDto]

  implicit val shipmentTypeDtoCodec: JsonCodec[ShipmentTypeDto] = DeriveJsonCodec.gen[ShipmentTypeDto]

  implicit val orderDetailDtoCodec: JsonCodec[OrderDetailDto] = DeriveJsonCodec.gen[OrderDetailDto]

  implicit val orderDtoCodec: JsonCodec[OrderDto] = DeriveJsonCodec.gen[OrderDto]

  // OrderRequestMessage

  implicit val orderRequestMessageCodec: JsonCodec[OrderRequestMessage] = DeriveJsonCodec.gen[OrderRequestMessage]

  implicit val orderRequestMessageSerde: Serde[Any, OrderRequestMessage] = deriveSerde[OrderRequestMessage]

  // OrderResponseMessage

  implicit val orderResponseMessageCodec: JsonCodec[OrderResponseMessage] = DeriveJsonCodec.gen[OrderResponseMessage]

  implicit val orderResponseMessageSerde: Serde[Any, OrderResponseMessage] = deriveSerde[OrderResponseMessage]

}
