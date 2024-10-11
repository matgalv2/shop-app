package io.github.g4lowy.http.acceptance

import io.github.g4lowy.http.AppEnvironment
import io.github.g4lowy.http.api.ClientApi
import zio._
import org.http4s._
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.circe._
import io.circe.{ Json, JsonObject }
import io.getquill.CamelCase
import io.getquill.jdbczio.Quill
import io.getquill.mirrorContextWithQueryProbing.{ querySchema, quote }
import io.github.g4lowy.client.infrastructure.model.ClientSQL

class ClientApiAcceptanceSpec extends ApiAcceptanceSpec {

  override val routes: ZIO[AppEnvironment, Nothing, HttpRoutes[RIO[AppEnvironment, *]]] = ClientApi.routes

  override def cleanTable: URIO[Quill.Postgres[CamelCase], Unit] =
    ZIO
      .serviceWithZIO[Quill.Postgres[CamelCase]] { quill =>
        quill.run(quote(querySchema[ClientSQL]("Clients").delete))
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

  "Clients Api handler" must {
    "return response 201 Created for creating client (POST '/clients')" in {
      Given("the request with valid data")
      val request = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = uri"/clients")
        .withEntity(validJson)

      When("the request is processed")

      val response = handleRequest(request)
      val body     = mapResponseBodyToJson(response)

      Then("the response should be 204 Created")
      response.status shouldBe Status.Created
      body.asObject.exists(_.contains("clientId")) shouldBe true

    }

    "return response 400 Bad request for creating client (POST '/clients')" in {
      Given("the request with invalid data")
      val request = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = uri"/clients")
        .withEntity(invalidJson)

      When("the request is processed")
      val response = handleRequest(request)

      Then("the response should be 400 Bad request")
      response.status shouldBe Status.BadRequest
    }

    "return response 200 Ok for fetching clients" in {
      Given("the request for fetching all clients")
      val request = Request[RIO[AppEnvironment, *]](method = Method.GET, uri = uri"/clients")

      When("the request is processed")
      val response = handleRequest(request)

      Then("the response should be 200 Ok")
      response.status shouldBe Status.Ok
    }

    "return response 200 Ok for fetching client by id" in {

      val createRequest = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = uri"/clients")
        .withEntity(validJson)

      val createResponse = handleRequest(createRequest)

      val createdClientId =
        mapResponseBodyToJson(createResponse).asObject
          .flatMap(_.apply("clientId"))
          .flatMap(_.asString)
          .getOrElse("")

      Given("the request for fetching existing client by id")
      val getByIdUri = Uri.fromString(s"/clients/$createdClientId").getOrElse(uri"/clients/id")
      val request    = Request[RIO[AppEnvironment, *]](method = Method.GET, uri = getByIdUri)

      When("the request is processed")
      val response = handleRequest(request)
      val body     = mapResponseBodyToJson(response)

      val returnedClientId  = body.asObject.flatMap(_.apply("clientId")).flatMap(_.asString).getOrElse("")
      val returnedFirstName = body.asObject.flatMap(_.apply("firstName")).flatMap(_.asString).getOrElse("")
      val returnedLastName  = body.asObject.flatMap(_.apply("lastName")).flatMap(_.asString).getOrElse("")
      val returnedBirthDate = body.asObject.flatMap(_.apply("birthDate")).flatMap(_.asString).getOrElse("")
      val returnedPhone     = body.asObject.flatMap(_.apply("phone")).flatMap(_.asString).getOrElse("")

      Then("the response should be 200 Ok")
      response.status shouldBe Status.Ok

      And("the response body should be equal to valid json")
      returnedClientId shouldEqual createdClientId
      returnedFirstName shouldEqual "FirstName"
      returnedLastName shouldEqual "LastName"
      returnedBirthDate shouldEqual "1996-08-08"
      returnedPhone shouldEqual "+48-123456789"
    }

    "return response 404 Not found for fetching client by id" in {
      Given("request with nonexistent id")
      val id         = nonExistentId
      val getByIdUri = Uri.fromString(s"/clients/$id").getOrElse(uri"/clients/id")
      val request    = Request[RIO[AppEnvironment, *]](method = Method.GET, uri = getByIdUri)
      When("the request is processed")
      val response = handleRequest(request)
      Then("the response should be 404 Not found")
      response.status shouldBe Status.NotFound
    }

    "return response 204 No content for updating client by id" in {
      val createRequest = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = uri"/clients")
        .withEntity(validJson)

      val createResponse = handleRequest(createRequest)

      val createdClientId =
        mapResponseBodyToJson(createResponse).asObject
          .flatMap(_.apply("clientId"))
          .flatMap(_.asString)
          .getOrElse("")

      Given("the request for updating existing client by id")

      val updateClientUri = Uri.fromString(s"/clients/$createdClientId").getOrElse(uri"")
      val updateRequest =
        Request[RIO[AppEnvironment, *]](method = Method.PUT, uri = updateClientUri).withEntity(validUpdateJson)

      When("the request is processed")

      val updateResponse = handleRequest(updateRequest)

      val getByIdUri        = Uri.fromString(s"/clients/$createdClientId").getOrElse(uri"/clients/id")
      val fetchRequest      = Request[RIO[AppEnvironment, *]](method = Method.GET, uri = getByIdUri)
      val fetchResponse     = handleRequest(fetchRequest)
      val fetchResponseBody = mapResponseBodyToJson(fetchResponse)

      val returnedClientId  = fetchResponseBody.asObject.flatMap(_.apply("clientId")).flatMap(_.asString).getOrElse("")
      val returnedFirstName = fetchResponseBody.asObject.flatMap(_.apply("firstName")).flatMap(_.asString).getOrElse("")
      val returnedLastName  = fetchResponseBody.asObject.flatMap(_.apply("lastName")).flatMap(_.asString).getOrElse("")
      val returnedBirthDate = fetchResponseBody.asObject.flatMap(_.apply("birthDate")).flatMap(_.asString).getOrElse("")
      val returnedPhone     = fetchResponseBody.asObject.flatMap(_.apply("phone")).flatMap(_.asString).getOrElse("")

      Then("the response should be 204 No content")
      updateResponse.status shouldBe Status.NoContent

      And("the response body should be equal to valid json")
      returnedClientId shouldEqual createdClientId
      returnedFirstName shouldEqual "Name"
      returnedLastName shouldEqual "Surname"
      returnedBirthDate shouldEqual "1996-08-09"
      returnedPhone shouldEqual "+48-123456"
    }

    "return response 400 Bad request for updating client by id" in {
      val createRequest = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = uri"/clients")
        .withEntity(validJson)

      val createResponse = handleRequest(createRequest)

      val createdClientId =
        mapResponseBodyToJson(createResponse).asObject
          .flatMap(_.apply("clientId"))
          .flatMap(_.asString)
          .getOrElse("")

      Given("the request for updating existing client by id with invalid body")

      val updateClientUri = Uri.fromString(s"/clients/$createdClientId").getOrElse(uri"")
      val updateRequest =
        Request[RIO[AppEnvironment, *]](method = Method.PUT, uri = updateClientUri).withEntity(invalidJson)

      When("the request is processed")
      val updateResponse = handleRequest(updateRequest)

      Then("the response should be 204 No content")
      updateResponse.status shouldBe Status.BadRequest
    }

    "return response 404 Not found for updating client by id" in {
      Given("the request for updating existing client by id with invalid body")

      val updateClientUri = Uri.fromString(s"/clients/$nonExistentId").getOrElse(uri"")
      val updateRequest =
        Request[RIO[AppEnvironment, *]](method = Method.PUT, uri = updateClientUri).withEntity(validUpdateJson)

      When("the request is processed")
      val updateResponse = handleRequest(updateRequest)

      Then("the response should be 404 Not found")
      updateResponse.status shouldBe Status.NotFound
    }

    "return response 204 No content for deleting client by id" in {
      val createRequest = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = uri"/clients")
        .withEntity(validJson)

      val createResponse = handleRequest(createRequest)

      val createdClientId =
        mapResponseBodyToJson(createResponse).asObject
          .flatMap(_.apply("clientId"))
          .flatMap(_.asString)
          .getOrElse("")

      val getByIdUri = Uri.fromString(s"/clients/$createdClientId").getOrElse(uri"/clients/id")

      Given("the request for deleting existing client by id")

      val deleteClientUri = Uri.fromString(s"/clients/$createdClientId").getOrElse(uri"")
      val deleteRequest =
        Request[RIO[AppEnvironment, *]](method = Method.DELETE, uri = deleteClientUri).withEntity(validUpdateJson)

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

//    "return response 400 Bad request for deleting client by id" in {
//      Given("")
//      When("")
//      Then("")
//    }

    "return response 404 Not found for deleting client by id" in {
      Given("the request for deleting nonexistent client by id")
      val deleteClientUri = Uri.fromString(s"/clients/$nonExistentId").getOrElse(uri"")
      val request         = Request[RIO[AppEnvironment, *]](method = Method.DELETE, uri = deleteClientUri)

      When("the request is processed")
      val response = handleRequest(request)

      Then("the response should be 404 Not found")
      response.status shouldBe Status.NotFound
    }
  }
}
