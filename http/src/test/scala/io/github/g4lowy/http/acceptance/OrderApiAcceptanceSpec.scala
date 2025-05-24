package io.github.g4lowy.http.acceptance

import http.generated.definitions.CreateOrder.{PaymentType, ShipmentType}
import http.generated.definitions.{GetOrder, PatchOrder}
import io.circe.{Json, JsonObject}
import io.getquill.CamelCase
import io.getquill.jdbczio.Quill
import io.github.g4lowy.customer.domain.model._
import io.github.g4lowy.customer.infrastructure.model.CustomerSQL
import io.github.g4lowy.http.AppEnvironment
import io.github.g4lowy.http.api.OrderApi
import io.github.g4lowy.order.infrastructure.database.model.{AddressSQL, OrderDetailSQL, OrderSQL}
import io.github.g4lowy.product.domain.model._
import io.github.g4lowy.product.infrastructure.model.ProductSQL
import io.github.g4lowy.testutils.validation.ValidationOps
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits.http4sLiteralsSyntax
import zio._

import java.sql.Date
import java.time.LocalDateTime
import java.util.UUID

class OrderApiAcceptanceSpec extends ApiAcceptanceSpec {

  override protected val routes: URIO[AppEnvironment, HttpRoutes[RIO[AppEnvironment, *]]] = OrderApi.routes

  protected def cleanData: URIO[Quill.Postgres[CamelCase], Unit] =
    ZIO
      .serviceWithZIO[Quill.Postgres[CamelCase]] { quill =>
        import quill._
        for {
          _ <- quill.run(quote(querySchema[OrderDetailSQL]("orders_details").delete))
          _ <- quill.run(quote(querySchema[OrderSQL]("orders").delete))
          _ <- quill.run(quote(querySchema[AddressSQL]("addresses").delete))
        } yield ()
      }
      .unit
      .orDie

  override protected def cleanOtherData: URIO[Quill.Postgres[CamelCase], Unit] =
    ZIO
      .serviceWithZIO[Quill.Postgres[CamelCase]] { quill =>
        import quill._
        for {
          _ <- quill.run(quote(querySchema[CustomerSQL]("Customers").delete))
          _ <- quill.run(quote(querySchema[ProductSQL]("Products").delete))
        } yield ()
      }
      .unit
      .orDie

  private val customerId = UUID.randomUUID()
  private val productId  = UUID.randomUUID()

  override protected def beforeAll: Unit = {
    super.beforeAll

    val effect: URIO[Quill.Postgres[CamelCase], Unit] = {

      val product  = ProductSQL.fromDomain(makeProduct(productId).validate.asValid)
      val customer = CustomerSQL.fromDomain(makeCustomer(customerId).validate.asValid)
      ZIO
        .serviceWithZIO[Quill.Postgres[CamelCase]] { quill =>
          import quill._
          for {
            _ <- quill.run(quote(querySchema[CustomerSQL]("Customers").insertValue(lift(customer))))
            _ <- quill.run(quote(querySchema[ProductSQL]("Products").insertValue(lift(product))))
          } yield ()
        }
        .unit
        .orDie
    }
    runEffect(effect.provide(dependencies))
  }

  private val addressJson = Json.fromJsonObject(
    JsonObject(
      "country" -> Json.fromString("Ireland"),
      "city" -> Json.fromString("Dublin"),
      "street" -> Json.fromString("Hillary"),
      "zipCode" -> Json.fromString("50-500"),
      "building" -> Json.fromString("12a"),
      "apartment" -> Json.fromString("12")
    )
  )

  private val validJson: Json = Json.fromJsonObject(
    JsonObject(
      "customerId" -> Json.fromString(customerId.toString),
      "details" -> Json.arr(
        Json
          .fromJsonObject(JsonObject("productId" -> Json.fromString(productId.toString), "quantity" -> Json.fromInt(1)))
      ),
      "paymentType" -> Json.fromString(PaymentType.BankTransfer.value),
      "paymentAddress" -> addressJson,
      "shipmentType" -> Json.fromString(ShipmentType.Courier.value),
      "shipmentAddress" -> addressJson
    )
  )

  private val validUpdateJson: Json =
    Json.fromJsonObject(JsonObject("status" -> Json.fromString(PatchOrder.Status.Delivered.toString)))

  private val inValidUpdateJson: Json =
    Json.fromJsonObject(JsonObject("status" -> Json.fromString(PatchOrder.Status.Cancelled.toString)))

  private val invalidJson = Json.fromJsonObject(
    JsonObject(
      "customerId" -> Json.fromString(customerId.toString),
      "details" -> Json.arr(
        Json.fromJsonObject(
          JsonObject("productId" -> Json.fromString(productId.toString), "quantity" -> Json.fromInt(-1))
        )
      ),
      "paymentType" -> Json.fromString(PaymentType.BankTransfer.value),
      "paymentAddress" -> addressJson,
      "shipmentType" -> Json.fromString(ShipmentType.Courier.value),
      "shipmentAddress" -> addressJson
    )
  )

  private val baseURL = uri"/orders"

  private val parametrizedURL = (id: String) => s"/orders/$id"

  private val returnedIdFieldName = "value"

  "Order API handler" must {

    "return response 201 Created for creating order (POST '/orders')" in {

      Given("the request with valid data")
      val request = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = baseURL)
        .withEntity(validJson)

      When("the request is processed")

      val response = handleRequest(request)
      val body     = mapResponseBodyToJson(response)

      Then("the response should be 204 Created")
      println(body.asObject)
      response.status shouldBe Status.Created
      body.asObject.exists(_.contains("value")) shouldBe true

    }

    "return response 400 Bad request for creating order (POST '/orders')" in {
      Given("the request with invalid data")
      val request = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = baseURL)
        .withEntity(invalidJson)

      When("the request is processed")
      val response = handleRequest(request)

      Then("the response should be 400 Bad request")
      response.status shouldBe Status.BadRequest
    }

    "return response 200 Ok for fetching orders (GET '/orders')" in {
      Given("the request for fetching all orders")
      val request = Request[RIO[AppEnvironment, *]](method = Method.GET, uri = baseURL)

      When("the request is processed")
      val response = handleRequest(request)

      Then("the response should be 200 Ok")
      response.status shouldBe Status.Ok
    }

    "return response 200 Ok for fetching order by id (GET '/orders/{orderId}')" in {

      val createRequest = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = baseURL)
        .withEntity(validJson)

      val createResponse = handleRequest(createRequest)

      val createdOrderId =
        mapResponseBodyToJson(createResponse).asObject
          .flatMap(_.apply(returnedIdFieldName))
          .flatMap(_.asString)
          .getOrElse("")

      Given("the request for fetching existing order by id")
      val getByIdUri = uriFromString(parametrizedURL(createdOrderId))
      val request    = Request[RIO[AppEnvironment, *]](method = Method.GET, uri = getByIdUri)

      When("the request is processed")
      val response = handleRequest(request)
      val body     = mapResponseBodyToJson(response)

      val returnedCustomerId   = body.asObject.flatMap(_.apply("customerId")).flatMap(_.asString).getOrElse("")
      val returnedStatus       = body.asObject.flatMap(_.apply("status")).flatMap(_.asString).getOrElse("")
      val returnedTotalCost    = body.asObject.flatMap(_.apply("totalCost")).flatMap(_.asNumber).getOrElse("")
      val returnedPaymentType  = body.asObject.flatMap(_.apply("paymentType")).flatMap(_.asString).getOrElse("")
      val returnedShipmentType = body.asObject.flatMap(_.apply("shipmentType")).flatMap(_.asString).getOrElse("")

      Then("the response should be 200 Ok")
      response.status shouldBe Status.Ok

      And("the response body should be equal to valid json")
      returnedCustomerId shouldEqual customerId.toString
      returnedStatus shouldEqual GetOrder.Status.Created.value
      returnedTotalCost.toString shouldEqual "3.59"
      returnedPaymentType shouldEqual PaymentType.BankTransfer.value
      returnedShipmentType shouldEqual ShipmentType.Courier.value
    }

    "return response 404 Not found for fetching order by id (GET '/orders/{orderId}')" in {

      Given("request with nonexistent id")
      val getByIdUri = uriFromString(parametrizedURL(nonExistentId))
      val request    = Request[RIO[AppEnvironment, *]](method = Method.GET, uri = getByIdUri)

      When("the request is processed")
      val response = handleRequest(request)

      Then("the response should be 404 Not found")
      response.status shouldBe Status.NotFound
    }

    "return response 204 No content for patching order by id (PUT '/orders/{orderId}')" in {

      val createRequest = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = baseURL)
        .withEntity(validJson)

      val createResponse = handleRequest(createRequest)

      val createdOrderId =
        mapResponseBodyToJson(createResponse).asObject
          .flatMap(_.apply(returnedIdFieldName))
          .flatMap(_.asString)
          .getOrElse("")

      Given("the request for updating existing order by id with valid body")

      val idUri = uriFromString(parametrizedURL(createdOrderId))
      val patchRequest = Request[RIO[AppEnvironment, *]](method = Method.PATCH, uri = idUri)
        .withEntity(validUpdateJson)

      When("the request is processed")
      val patchResponse = handleRequest(patchRequest)

      Then("the response should be 204 No content")
      patchResponse.status shouldBe Status.NoContent

    }

    "return response 404 Not found for patching order by id (PUT '/orders/{orderId}')" in {

      Given("the request for updating order's status by id with valid body")

      val updateCustomerUri = uriFromString(parametrizedURL(nonExistentId))
      val updateRequest =
        Request[RIO[AppEnvironment, *]](method = Method.PATCH, uri = updateCustomerUri).withEntity(validUpdateJson)

      When("the request is processed")
      val updateResponse = handleRequest(updateRequest)

      Then("the response should be 404 Not found")
      updateResponse.status shouldBe Status.NotFound
    }

    "return response 400 Bad request for patching order by id (PUT '/orders/{orderId}')" in {
      val createRequest = Request[RIO[AppEnvironment, *]](method = Method.POST, uri = baseURL)
        .withEntity(validJson)

      val createResponse = handleRequest(createRequest)

      val createdOrderId =
        mapResponseBodyToJson(createResponse).asObject
          .flatMap(_.apply(returnedIdFieldName))
          .flatMap(_.asString)
          .getOrElse("")

      Given("the request for updating existing order by id with invalid body")

      val idUri = uriFromString(parametrizedURL(createdOrderId))

      val correctPatchRequest = Request[RIO[AppEnvironment, *]](method = Method.PATCH, uri = idUri)
        .withEntity(validUpdateJson)

      handleRequest(correctPatchRequest)

      val incorrectPatchRequest = Request[RIO[AppEnvironment, *]](method = Method.PATCH, uri = idUri)
        .withEntity(inValidUpdateJson)

      When("the request is processed")
      val incorrectPatchResponse = handleRequest(incorrectPatchRequest)

      Then("the response should be 400 Bad request")
      incorrectPatchResponse.status shouldBe Status.BadRequest
    }
  }

  private def makeProduct(
    id: UUID,
    name: String                = "Product",
    price: Double               = 3.59,
    description: Option[String] = None
  ): Product.Unvalidated =
    Product.Unvalidated(
      ProductId.fromUUID(id),
      Name.Unvalidated(name),
      Price.Unvalidated(price),
      description.map(Description.Unvalidated.apply)
    )

  private def makeCustomer(id: UUID) =
    Customer.Unvalidated(
      customerId = CustomerId.fromUUID(id),
      firstName  = FirstName.Unvalidated("Rufus"),
      lastName   = LastName.Unvalidated("Goldenek"),
      birthDate  = Some(Date.valueOf("2009-03-05")),
      phone      = Phone.Unvalidated("+48-20241026"),
      createdAt  = LocalDateTime.now()
    )
}
