package io.github.g4lowy.http.acceptance

import io.circe.{Json, JsonObject}
import io.getquill.CamelCase
import io.getquill.jdbczio.Quill
import io.getquill.mirrorContextWithQueryProbing.{querySchema, quote}
import io.github.g4lowy.customer.infrastructure.model.CustomerSQL
import io.github.g4lowy.http.AppEnvironment
import io.github.g4lowy.http.api.CustomerApi
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits.http4sLiteralsSyntax
import zio._

class CustomerApiAcceptanceSpec extends ApiAcceptanceSpec {

  override protected val routes: ZIO[AppEnvironment, Nothing, HttpRoutes[RIO[AppEnvironment, *]]] = CustomerApi.routes

  override protected def cleanData: URIO[Quill.Postgres[CamelCase], Unit] =
    ZIO
      .serviceWithZIO[Quill.Postgres[CamelCase]] { quill =>
        quill.run(quote(querySchema[CustomerSQL]("Customers").delete))
      }
      .unit
      .orDie

  private val validJson: Json = Json.fromJsonObject(
    JsonObject(
      "firstName" -> Json.fromString("FirstName"),
      "lastName" -> Json.fromString("LastName"),
      "birthDate" -> Json.fromString("1996-08-08"),
      "phone" -> Json.fromString("+48-123456789")
    )
  )

  private val validUpdateJson: Json = Json.fromJsonObject(
    JsonObject(
      "firstName" -> Json.fromString("Name"),
      "lastName" -> Json.fromString("Surname"),
      "birthDate" -> Json.fromString("1996-08-09"),
      "phone" -> Json.fromString("+48-123456")
    )
  )

  private val invalidJson = Json.fromJsonObject(
    JsonObject(
      "firstName" -> Json.fromString("lowerCase"),
      "lastName" -> Json.fromString("LastName"),
      "phone" -> Json.fromString("+48123456789")
    )
  )

  private val nonExistentId = "99999999-9999-9999-9999-2a035d9e16ba"

  "Customer API handler" must {
    "return response 201 Created for creating customer (POST '/customers')" in {
      Given("the request with valid data")
      val request = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = uri"/customers")
        .withEntity(validJson)

      When("the request is processed")

      val response = handleRequest(request)
      val body     = mapResponseBodyToJson(response)

      Then("the response should be 204 Created")
      response.status shouldBe Status.Created
      body.asObject.exists(_.contains("value")) shouldBe true

    }

    "return response 400 Bad request for creating customer (POST '/customers')" in {
      Given("the request with invalid data")
      val request = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = uri"/customers")
        .withEntity(invalidJson)

      When("the request is processed")
      val response = handleRequest(request)

      Then("the response should be 400 Bad request")
      response.status shouldBe Status.BadRequest
    }

    "return response 200 Ok for fetching customers (GET '/customers')" in {
      Given("the request for fetching all customers")
      val request = Request[RIO[AppEnvironment, *]](method = Method.GET, uri = uri"/customers")

      When("the request is processed")
      val response = handleRequest(request)

      Then("the response should be 200 Ok")
      response.status shouldBe Status.Ok
    }

    "return response 200 Ok for fetching customer by id (GET '/customers/{customerId}')" in {

      val createRequest = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = uri"/customers")
        .withEntity(validJson)

      val createResponse = handleRequest(createRequest)

      val createdCustomerId =
        mapResponseBodyToJson(createResponse).asObject
          .flatMap(_.apply("value"))
          .flatMap(_.asString)
          .getOrElse("")

      Given("the request for fetching existing customer by id")
      val getByIdUri = Uri.fromString(s"/customers/$createdCustomerId").getOrElse(uri"/customers/id")
      val request    = Request[RIO[AppEnvironment, *]](method = Method.GET, uri = getByIdUri)

      When("the request is processed")
      val response = handleRequest(request)
      val body     = mapResponseBodyToJson(response)

      val returnedCustomerId = body.asObject.flatMap(_.apply("customerId")).flatMap(_.asString).getOrElse("")
      val returnedFirstName  = body.asObject.flatMap(_.apply("firstName")).flatMap(_.asString).getOrElse("")
      val returnedLastName   = body.asObject.flatMap(_.apply("lastName")).flatMap(_.asString).getOrElse("")
      val returnedBirthDate  = body.asObject.flatMap(_.apply("birthDate")).flatMap(_.asString).getOrElse("")
      val returnedPhone      = body.asObject.flatMap(_.apply("phone")).flatMap(_.asString).getOrElse("")

      Then("the response should be 200 Ok")
      response.status shouldBe Status.Ok

      And("the response body should be equal to valid json")
      returnedCustomerId shouldEqual createdCustomerId
      returnedFirstName shouldEqual "FirstName"
      returnedLastName shouldEqual "LastName"
      returnedBirthDate shouldEqual "1996-08-08"
      returnedPhone shouldEqual "+48-123456789"
    }

    "return response 404 Not found for fetching customer by id (GET '/customers/{customerId}')" in {
      Given("request with nonexistent id")
      val id         = nonExistentId
      val getByIdUri = Uri.fromString(s"/customers/$id").getOrElse(uri"/customers/id")
      val request    = Request[RIO[AppEnvironment, *]](method = Method.GET, uri = getByIdUri)
      When("the request is processed")
      val response = handleRequest(request)
      Then("the response should be 404 Not found")
      response.status shouldBe Status.NotFound
    }

    "return response 204 No content for updating customer by id (PUT '/customers/{customerId}')" in {
      val createRequest = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = uri"/customers")
        .withEntity(validJson)

      val createResponse = handleRequest(createRequest)

      val createdCustomerId =
        mapResponseBodyToJson(createResponse).asObject
          .flatMap(_.apply("value"))
          .flatMap(_.asString)
          .getOrElse("")

      Given("the request for updating existing customer by id")

      val updateCustomerUri = Uri.fromString(s"/customers/$createdCustomerId").getOrElse(uri"")
      val updateRequest =
        Request[RIO[AppEnvironment, *]](method = Method.PUT, uri = updateCustomerUri).withEntity(validUpdateJson)

      When("the request is processed")

      val updateResponse = handleRequest(updateRequest)

      val getByIdUri        = Uri.fromString(s"/customers/$createdCustomerId").getOrElse(uri"/customers/id")
      val fetchRequest      = Request[RIO[AppEnvironment, *]](method = Method.GET, uri = getByIdUri)
      val fetchResponse     = handleRequest(fetchRequest)
      val fetchResponseBody = mapResponseBodyToJson(fetchResponse)

      val returnedCustomerId =
        fetchResponseBody.asObject.flatMap(_.apply("customerId")).flatMap(_.asString).getOrElse("")
      val returnedFirstName = fetchResponseBody.asObject.flatMap(_.apply("firstName")).flatMap(_.asString).getOrElse("")
      val returnedLastName  = fetchResponseBody.asObject.flatMap(_.apply("lastName")).flatMap(_.asString).getOrElse("")
      val returnedBirthDate = fetchResponseBody.asObject.flatMap(_.apply("birthDate")).flatMap(_.asString).getOrElse("")
      val returnedPhone     = fetchResponseBody.asObject.flatMap(_.apply("phone")).flatMap(_.asString).getOrElse("")

      Then("the response should be 204 No content")
      updateResponse.status shouldBe Status.NoContent

      And("the response body should be equal to valid json")
      returnedCustomerId shouldEqual createdCustomerId
      returnedFirstName shouldEqual "Name"
      returnedLastName shouldEqual "Surname"
      returnedBirthDate shouldEqual "1996-08-09"
      returnedPhone shouldEqual "+48-123456"
    }

    "return response 400 Bad request for updating customer by id (PUT '/customers/{customerId}')" in {
      val createRequest = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = uri"/customers")
        .withEntity(validJson)

      val createResponse = handleRequest(createRequest)

      val createdCustomerId =
        mapResponseBodyToJson(createResponse).asObject
          .flatMap(_.apply("value"))
          .flatMap(_.asString)
          .getOrElse("")

      Given("the request for updating existing customer by id with invalid body")

      val updateCustomerUri = Uri.fromString(s"/customers/$createdCustomerId").getOrElse(uri"")
      val updateRequest =
        Request[RIO[AppEnvironment, *]](method = Method.PUT, uri = updateCustomerUri).withEntity(invalidJson)

      When("the request is processed")
      val updateResponse = handleRequest(updateRequest)

      Then("the response should be 204 No content")
      updateResponse.status shouldBe Status.BadRequest
    }

    "return response 404 Not found for updating customer by id (PUT '/customers/{customerId}')" in {
      Given("the request for updating existing customer by id with invalid body")

      val updateCustomerUri = Uri.fromString(s"/customers/$nonExistentId").getOrElse(uri"")
      val updateRequest =
        Request[RIO[AppEnvironment, *]](method = Method.PUT, uri = updateCustomerUri).withEntity(validUpdateJson)

      When("the request is processed")
      val updateResponse = handleRequest(updateRequest)

      Then("the response should be 404 Not found")
      updateResponse.status shouldBe Status.NotFound
    }

    "return response 204 No content for deleting customer by id (DELETE '/customers/{customerId}')" in {
      val createRequest = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = uri"/customers")
        .withEntity(validJson)

      val createResponse = handleRequest(createRequest)

      val createdCustomerId =
        mapResponseBodyToJson(createResponse).asObject
          .flatMap(_.apply("value"))
          .flatMap(_.asString)
          .getOrElse("")

      val getByIdUri = Uri.fromString(s"/customers/$createdCustomerId").getOrElse(uri"/customers/id")

      Given("the request for deleting existing customer by id")

      val deleteCustomerUri = Uri.fromString(s"/customers/$createdCustomerId").getOrElse(uri"")
      val deleteRequest =
        Request[RIO[AppEnvironment, *]](method = Method.DELETE, uri = deleteCustomerUri).withEntity(validUpdateJson)

      When("the request is processed")
      val fetchBeforeDeleteRequest  = Request[RIO[AppEnvironment, *]](method = Method.GET, uri = getByIdUri)
      val fetchBeforeDeleteResponse = handleRequest(fetchBeforeDeleteRequest)

      val deleteResponse = handleRequest(deleteRequest)

      val fetchAfterDeleteRequest  = Request[RIO[AppEnvironment, *]](method = Method.GET, uri = getByIdUri)
      val fetchAfterDeleteResponse = handleRequest(fetchAfterDeleteRequest)

      Then("the response should be 204 No content")
      fetchBeforeDeleteResponse.status shouldBe Status.Ok
      deleteResponse.status shouldBe Status.NoContent
      fetchAfterDeleteResponse.status shouldBe Status.NotFound
    }

//    "return response 400 Bad request for deleting customer by id" in {
//      Given("")
//      When("")
//      Then("")
//    }

    "return response 404 Not found for deleting customer by id (DELETE '/customers/{customerId}')" in {
      Given("the request for deleting nonexistent customer by id")
      val deleteCustomerUri = Uri.fromString(s"/customers/$nonExistentId").getOrElse(uri"")
      val request           = Request[RIO[AppEnvironment, *]](method = Method.DELETE, uri = deleteCustomerUri)

      When("the request is processed")
      val response = handleRequest(request)

      Then("the response should be 404 Not found")
      response.status shouldBe Status.NotFound
    }
  }
}
