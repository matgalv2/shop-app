package io.github.g4lowy.http.acceptance
import io.circe.{Json, JsonObject}
import io.getquill.CamelCase
import io.getquill.jdbczio.Quill
import io.getquill.mirrorContextWithQueryProbing.{querySchema, quote}
import io.github.g4lowy.http.AppEnvironment
import io.github.g4lowy.http.api.ProductApi
import io.github.g4lowy.product.infrastructure.model.ProductSQL
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits.http4sLiteralsSyntax
import zio.{RIO, URIO, ZIO}

class ProductApiAcceptanceSpec extends ApiAcceptanceSpec {

  override protected val routes: ZIO[AppEnvironment, Nothing, HttpRoutes[RIO[AppEnvironment, *]]] = ProductApi.routes

  override protected def cleanData: URIO[Quill.Postgres[CamelCase], Unit] =
    ZIO
      .serviceWithZIO[Quill.Postgres[CamelCase]] { quill =>
        quill.run(quote(querySchema[ProductSQL]("Products").delete))
      }
      .unit
      .orDie

  private val validJson: Json = Json.fromJsonObject(
    JsonObject(
      "name" -> Json.fromString("product1"),
      "price" -> Json.fromDoubleOrNull(3.59),
      "description" -> Json.fromString("description")
    )
  )

  private val validUpdateJson: Json =
    Json.fromJsonObject(JsonObject("name" -> Json.fromString("updated"), "price" -> Json.fromDoubleOrNull(3.59)))

  private val invalidJson = Json.fromJsonObject(
    JsonObject(
      "name" -> Json.fromString("lowerCase"),
      "price" -> Json.fromDoubleOrNull(-1.3),
      "description" -> Json.fromString("description")
    )
  )

  private val nonExistentId = "99999999-9999-9999-9999-2a035d9e16ba"

  "Product API handler" must {
    "return response 201 Created for creating product (POST '/products')" in {
      Given("the request with valid data")
      val request = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = uri"/products")
        .withEntity(validJson)

      When("the request is processed")

      val response = handleRequest(request)
      val body     = mapResponseBodyToJson(response)

      Then("the response should be 204 Created")
      response.status shouldBe Status.Created
      body.asObject.exists(_.contains("value")) shouldBe true

    }

    "return response 400 Bad request for creating product (POST '/products')" in {
      Given("the request with invalid data")
      val request = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = uri"/products")
        .withEntity(invalidJson)

      When("the request is processed")
      val response = handleRequest(request)

      Then("the response should be 400 Bad request")
      response.status shouldBe Status.BadRequest
    }

    "return response 200 Ok for fetching clients (GET '/products')" in {
      Given("the request for fetching all clients")
      val request = Request[RIO[AppEnvironment, *]](method = Method.GET, uri = uri"/products")

      When("the request is processed")
      val response = handleRequest(request)

      Then("the response should be 200 Ok")
      response.status shouldBe Status.Ok
    }

    "return response 200 Ok for fetching product by id (GET '/products/{productId}')" in {

      val createRequest = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = uri"/products")
        .withEntity(validJson)

      val createResponse = handleRequest(createRequest)

      val createdProductId =
        mapResponseBodyToJson(createResponse).asObject
          .flatMap(_.apply("value"))
          .flatMap(_.asString)
          .getOrElse("")

      Given("the request for fetching existing client by id")
      val getByIdUri = Uri.fromString(s"/products/$createdProductId").getOrElse(uri"/products/id")
      val request    = Request[RIO[AppEnvironment, *]](method = Method.GET, uri = getByIdUri)

      When("the request is processed")
      val response = handleRequest(request)
      val body     = mapResponseBodyToJson(response)

      val returnedProductId   = body.asObject.flatMap(_.apply("productId")).flatMap(_.asString).getOrElse("")
      val returnedName        = body.asObject.flatMap(_.apply("name")).flatMap(_.asString).getOrElse("")
      val returnedPrice       = body.asObject.flatMap(_.apply("price")).flatMap(_.asNumber.map(_.toDouble)).getOrElse(-1d)
      val returnedDescription = body.asObject.flatMap(_.apply("description")).flatMap(_.asString).getOrElse("")

      Then("the response should be 200 Ok")
      response.status shouldBe Status.Ok

      And("the response body should be equal to valid json")
      returnedProductId shouldEqual createdProductId
      returnedName shouldEqual "product1"
      returnedPrice shouldEqual 3.59
      returnedDescription shouldEqual "description"
    }

    "return response 404 Not found for fetching product by id (GET '/products/{productId}')" in {
      Given("request with nonexistent id")
      val id         = nonExistentId
      val getByIdUri = Uri.fromString(s"/products/$id").getOrElse(uri"/products/id")
      val request    = Request[RIO[AppEnvironment, *]](method = Method.GET, uri = getByIdUri)

      When("the request is processed")
      val response = handleRequest(request)

      Then("the response should be 404 Not found")
      response.status shouldBe Status.NotFound
    }

    "return response 204 No content for updating product by id (PUT '/products/{productId}')" in {
      val createRequest = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = uri"/products")
        .withEntity(validJson)

      val createResponse = handleRequest(createRequest)

      val createdProductId =
        mapResponseBodyToJson(createResponse).asObject
          .flatMap(_.apply("value"))
          .flatMap(_.asString)
          .getOrElse("")

      Given("the request for updating existing product by id")

      val updateUri = Uri.fromString(s"/products/$createdProductId").getOrElse(uri"")
      val updateRequest =
        Request[RIO[AppEnvironment, *]](method = Method.PUT, uri = updateUri).withEntity(validUpdateJson)

      When("the request is processed")

      val updateResponse = handleRequest(updateRequest)

      val getByIdUri        = Uri.fromString(s"/products/$createdProductId").getOrElse(uri"/products/id")
      val fetchRequest      = Request[RIO[AppEnvironment, *]](method = Method.GET, uri = getByIdUri)
      val fetchResponse     = handleRequest(fetchRequest)
      val fetchResponseBody = mapResponseBodyToJson(fetchResponse)

      val returnedProductId = fetchResponseBody.asObject.flatMap(_.apply("productId")).flatMap(_.asString).getOrElse("")
      val returnedName      = fetchResponseBody.asObject.flatMap(_.apply("name")).flatMap(_.asString).getOrElse("")
      val returnedPrice =
        fetchResponseBody.asObject.flatMap(_.apply("price")).flatMap(_.asNumber.map(_.toDouble)).getOrElse(-1d)
      val returnedDescription =
        fetchResponseBody.asObject.flatMap(_.apply("description")).getOrElse(Json.fromString("null"))

      Then("the response should be 204 No content")
      updateResponse.status shouldBe Status.NoContent

      And("the response body should be equal to valid json")
      returnedProductId shouldEqual createdProductId
      returnedName shouldEqual "updated"
      returnedDescription shouldEqual Json.Null
      returnedPrice shouldEqual 3.59d
    }

    "return response 400 Bad request for updating product by id (PUT '/products/{productId}')" in {
      val createRequest = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = uri"/products")
        .withEntity(validJson)

      val createResponse = handleRequest(createRequest)

      val createProductId =
        mapResponseBodyToJson(createResponse).asObject
          .flatMap(_.apply("value"))
          .flatMap(_.asString)
          .getOrElse("")

      Given("the request for updating existing product by id with invalid body")

      val updateUri = Uri.fromString(s"/products/$createProductId").getOrElse(uri"")
      val updateRequest =
        Request[RIO[AppEnvironment, *]](method = Method.PUT, uri = updateUri).withEntity(invalidJson)

      When("the request is processed")
      val updateResponse = handleRequest(updateRequest)

      Then("the response should be 204 No content")
      updateResponse.status shouldBe Status.BadRequest
    }

    "return response 404 Not found for updating product by id (PUT '/products/{productId}')" in {
      Given("the request for updating existing product by id with invalid body")

      val updateUri = Uri.fromString(s"/products/$nonExistentId").getOrElse(uri"")
      val updateRequest =
        Request[RIO[AppEnvironment, *]](method = Method.PUT, uri = updateUri).withEntity(validUpdateJson)

      When("the request is processed")
      val updateResponse = handleRequest(updateRequest)

      Then("the response should be 404 Not found")
      updateResponse.status shouldBe Status.NotFound
    }

    "return response 204 No content for deleting product by id (DELETE '/products/{productId}')" in {
      val createRequest = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = uri"/products")
        .withEntity(validJson)

      val createResponse = handleRequest(createRequest)

      val createProductId =
        mapResponseBodyToJson(createResponse).asObject
          .flatMap(_.apply("value"))
          .flatMap(_.asString)
          .getOrElse("")

      val idUri = Uri.fromString(s"/products/$createProductId").getOrElse(uri"")

      Given("the request for deleting existing product by id")

      val deleteRequest =
        Request[RIO[AppEnvironment, *]](method = Method.DELETE, uri = idUri).withEntity(validUpdateJson)

      When("the request is processed")
      val fetchBeforeDeleteRequest  = Request[RIO[AppEnvironment, *]](method = Method.GET, uri = idUri)
      val fetchBeforeDeleteResponse = handleRequest(fetchBeforeDeleteRequest)

      val deleteResponse = handleRequest(deleteRequest)

      val fetchAfterDeleteRequest  = Request[RIO[AppEnvironment, *]](method = Method.GET, uri = idUri)
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

    "return response 404 Not found for deleting product by id (DELETE '/products/{productId}')" in {
      Given("the request for deleting nonexistent product by id")
      val idUri   = Uri.fromString(s"/products/$nonExistentId").getOrElse(uri"")
      val request = Request[RIO[AppEnvironment, *]](method = Method.DELETE, uri = idUri)

      When("the request is processed")
      val response = handleRequest(request)

      Then("the response should be 404 Not found")
      response.status shouldBe Status.NotFound
    }
  }

}
