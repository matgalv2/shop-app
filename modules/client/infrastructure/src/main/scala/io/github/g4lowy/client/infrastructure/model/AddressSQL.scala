package io.github.g4lowy.client.infrastructure.model

import io.github.g4lowy.client.domain.model.Address
import io.github.g4lowy.client.domain.model.Address._

final case class AddressSQL(
                             country: String,
                             city: String,
                             street: String,
                             zipCode: String,
                             building: String,
                             apartment: Option[String]){

  def toUnvalidated: Address.Unvalidated =
    Address.Unvalidated(
      country = Country.Unvalidated(country),
      city    = City.Unvalidated(city),
      street  = Street.Unvalidated(street),
      zipCode = ZipCode.Unvalidated(zipCode),
      building = Building.Unvalidated(building),
      apartment = apartment.map(Apartment.Unvalidated.apply)
    )
}

object AddressSQL {
  def fromDomain(address: Address): AddressSQL =
    AddressSQL(
      country = address.country.value,
      city = address.city.value,
      street = address.street.value,
      zipCode = address.zipCode.value,
      building = address.building.value,
      apartment = address.apartment.map(_.value)
    )
}
