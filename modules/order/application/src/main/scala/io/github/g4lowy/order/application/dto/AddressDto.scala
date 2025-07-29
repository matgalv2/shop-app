package io.github.g4lowy.order.application.dto

final case class AddressDto(
  country: String,
  city: String,
  street: String,
  zipCode: String,
  building: String,
  apartment: Option[String] = None
)
