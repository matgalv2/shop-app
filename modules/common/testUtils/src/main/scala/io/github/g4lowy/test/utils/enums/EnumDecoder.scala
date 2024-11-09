package io.github.g4lowy.test.utils.enums

trait EnumDecoder[T <: EnumSQL] {

  protected val values: List[T]

  def decode(value: String): T = {
    val result = values.find(_.value == value)

    if (result.isDefined)
      result.get
    else
      throw EnumDecodingError(
        s"Value $value was not found for type ${values.map(_.getClass.getSimpleName).headOption.getOrElse("Unknown")}."
      )
  }

}
