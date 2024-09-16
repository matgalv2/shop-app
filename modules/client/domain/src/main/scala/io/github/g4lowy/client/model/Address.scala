package io.github.g4lowy.client.model

import io.github.g4lowy.validation.validators.Validation
import Address._
import io.github.g4lowy.validation.extras.NotValidated
import io.github.g4lowy.validation.validators.Validator.FailureDescription

import io.github.g4lowy.validation.validators.Validator._

case class Address private(country: Country, city: City, street: Street, zipCode:ZipCode, building: Building, apartment: Option[Apartment])

object Address{

  final case class Unvalidated(country: Country.Unvalidated,
                               city: City.Unvalidated,
                               street: Street.Unvalidated,
                               zipCode: ZipCode.Unvalidated,
                               building: Building.Unvalidated,
                               apartment: Option[Apartment.Unvalidated]
                              ) extends NotValidated[Address] {

    override def validate: Validation[FailureDescription, Address] =
      for {
        countryV <- country.validate
        cityV <- city.validate
        streetV <- street.validate
        zipCodeV <- zipCode.validate
        buildingV <- building.validate
        apartmentV <- validOrCheck[Apartment, Apartment.Unvalidated](apartment)
      } yield Address(
        country = countryV,
        city = cityV,
        street = streetV,
        zipCode = zipCodeV,
        building = buildingV,
        apartment = apartmentV
      )

    override def unsafeValidation: Address =
      Address(
        country = country.unsafeValidation,
        city = city.unsafeValidation,
        street = street.unsafeValidation,
        zipCode = zipCode.unsafeValidation,
        building = building.unsafeValidation,
        apartment = apartment.map(_.unsafeValidation)
      )

  }

  final case class Country private(value: String)
  object Country{
    final case class Unvalidated(value: String) extends NotValidated[Country] {
      override def validate: Validation[FailureDescription, Country] =
        (nonEmpty and nonBlank and capitalized).apply(value).map(Country.apply)

      override def unsafeValidation: Country = Country.apply(value)
    }
  }

  final case class City private(value: String)
  object City{
    final case class Unvalidated(value: String) extends NotValidated[City] {
      override def validate: Validation[FailureDescription, City] =
        (nonEmpty and nonBlank and capitalized).apply(value).map(City.apply)

      override def unsafeValidation: City = City.apply(value)
    }
  }

  final case class Street private(value: String)
  object Street{
    final case class Unvalidated(value: String) extends NotValidated[Street] {
      override def validate: Validation[FailureDescription, Street] =
        (nonEmpty and nonBlank and capitalized).apply(value).map(Street.apply)

      override def unsafeValidation: Street = Street.apply(value)
    }
  }

  final case class ZipCode private(value: String)
  object ZipCode{
    final case class Unvalidated(value: String) extends NotValidated[ZipCode] {
      override def validate: Validation[FailureDescription, ZipCode] =
        matchesRegex("^[0-9]{2}-[0-9]{3}$".r).apply(value).map(ZipCode.apply)

      override def unsafeValidation: ZipCode = ZipCode.apply(value)
    }
  }


  final case class Building private(value: String)
  object Building{
    final case class Unvalidated(value: String) extends NotValidated[Building] {
      override def validate: Validation[FailureDescription, Building] =
        matchesRegex("^[0-9]*[A-z]*$".r).apply(value).map(Building.apply)

      override def unsafeValidation: Building = Building.apply(value)
    }
  }

  final case class Apartment private(value: String)
  object Apartment{
    final case class Unvalidated(value: String) extends NotValidated[Apartment] {
      override def validate: Validation[FailureDescription, Apartment] =
        matchesRegex("^[0-9]*[A-z]*$".r).apply(value).map(Apartment.apply)

      override def unsafeValidation: Apartment = Apartment.apply(value)
    }
  }

}
