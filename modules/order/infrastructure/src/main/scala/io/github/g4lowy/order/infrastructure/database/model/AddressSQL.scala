package io.github.g4lowy.order.infrastructure.database.model

import io.github.g4lowy.order.domain.model.address._

import java.util.UUID

final case class AddressSQL(
  addressId: UUID,
  country: String,
  city: String,
  street: String,
  zipCode: String,
  building: String,
  apartment: Option[String]
) {

  def toUnvalidated: Address.Unvalidated =
    Address.Unvalidated(
      addressId = AddressId.fromUUID(addressId),
      country   = Country.Unvalidated(country),
      city      = City.Unvalidated(city),
      street    = Street.Unvalidated(street),
      zipCode   = ZipCode.Unvalidated(zipCode),
      building  = Building.Unvalidated(building),
      apartment = apartment.map(Apartment.Unvalidated.apply)
    )
}

object AddressSQL {
  def fromDomain(address: Address): AddressSQL =
    AddressSQL(
      addressId = address.addressId.value,
      country   = address.country.value,
      city      = address.city.value,
      street    = address.street.value,
      zipCode   = address.zipCode.value,
      building  = address.building.value,
      apartment = address.apartment.map(_.value)
    )
}
