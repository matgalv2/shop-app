package io.github.g4lowy.order.domain.model.address

import io.github.g4lowy.validation.validators.Validator._
import io.github.g4lowy.validation.validators.{NotValidated, Validation}

final case class Address private (
  addressId: AddressId,
  country: Country,
  city: City,
  street: Street,
  zipCode: ZipCode,
  building: Building,
  apartment: Option[Apartment]
)

object Address {

  final case class Unvalidated(
    addressId: AddressId,
    country: Country.Unvalidated,
    city: City.Unvalidated,
    street: Street.Unvalidated,
    zipCode: ZipCode.Unvalidated,
    building: Building.Unvalidated,
    apartment: Option[Apartment.Unvalidated]
  ) extends NotValidated[Address] {

    override def validate: Validation[FailureDescription, Address] =
      for {
        countryV   <- country.validate
        cityV      <- city.validate
        streetV    <- street.validate
        zipCodeV   <- zipCode.validate
        buildingV  <- building.validate
        apartmentV <- validOrCheck[Apartment, Apartment.Unvalidated](apartment)
      } yield Address(
        addressId = addressId,
        country   = countryV,
        city      = cityV,
        street    = streetV,
        zipCode   = zipCodeV,
        building  = buildingV,
        apartment = apartmentV
      )

    override protected def unsafeValidation: Address =
      Address(
        addressId = addressId,
        country   = country.validateUnsafe,
        city      = city.validateUnsafe,
        street    = street.validateUnsafe,
        zipCode   = zipCode.validateUnsafe,
        building  = building.validateUnsafe,
        apartment = apartment.map(_.validateUnsafe)
      )

    private[order] def validateUnsafe: Address = unsafeValidation
  }
}
