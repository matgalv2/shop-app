package io.github.g4lowy.order.infrastructure.repository

import io.getquill.CamelCase
import io.getquill.jdbczio.Quill
import io.github.g4lowy.abstractType.Id.UUIDOps
import io.github.g4lowy.order.domain.model.{Order, OrderError, OrderId, OrderStatus}
import io.github.g4lowy.order.domain.repository.OrderRepository
import io.github.g4lowy.order.infrastructure.model._
import io.github.g4lowy.product.infrastructure.model.ProductSQL
import org.postgresql.util.PGobject
import zio.{IO, UIO, URLayer, ZIO, ZLayer}

import java.sql.{SQLException, Types}
import java.util.UUID

case class OrderRepositoryPostgres(quill: Quill.Postgres[CamelCase]) extends OrderRepository {

  import quill._

  private def ordersOffsetAndLimit(offset: Int, limit: Int) =
    quote {
      querySchema[OrderSQL]("Orders").drop(lift(offset)).take(lift(limit))
    }

  private val orders = quote {
    querySchema[OrderSQL]("Orders")
  }

  private val addresses = quote {
    querySchema[AddressSQL]("Addresses")
  }

  private val ordersDetails = quote {
    querySchema[OrderDetailSQL]("OrdersDetails")
  }

  private val products = quote {
    querySchema[ProductSQL]("Products")
  }

  private implicit val orderStatusEncoder: Encoder[OrderStatusSQL] = encoder[OrderStatusSQL](
    Types.OTHER,
    (index: Index, status: OrderStatusSQL, row: PrepareRow) => {
      val pgObj = new PGobject()
      pgObj.setType("ORDER_STATUS")
      pgObj.setValue(status.value)
      row.setObject(index, pgObj, Types.OTHER)
    }
  )

  private implicit val orderStatusDecoder: Decoder[OrderStatusSQL] = decoder[OrderStatusSQL] {
    (index: Index, row: ResultRow, _: Session) =>
      OrderStatusSQL.decode(row.getString(index))
  }

  private implicit val paymentTypeEncoder: Encoder[PaymentTypeSQL] = encoder[PaymentTypeSQL](
    Types.OTHER,
    (index: Index, status: PaymentTypeSQL, row: PrepareRow) => {
      val pgObj = new PGobject()
      pgObj.setType("PAYMENT_TYPE")
      pgObj.setValue(status.value)
      row.setObject(index, pgObj, Types.OTHER)
    }
  )

  private implicit val paymentTypeDecoder: Decoder[PaymentTypeSQL] = decoder[PaymentTypeSQL] {
    (index: Index, row: ResultRow, _: Session) =>
      PaymentTypeSQL.decode(row.getString(index))
  }

  private implicit val shipmentTypeEncoder: Encoder[ShipmentTypeSQL] = encoder[ShipmentTypeSQL](
    Types.OTHER,
    (index: Index, status: ShipmentTypeSQL, row: PrepareRow) => {
      val pgObj = new PGobject()
      pgObj.setType("SHIPMENT_TYPE")
      pgObj.setValue(status.value)
      row.setObject(index, pgObj, Types.OTHER)
    }
  )

  private implicit val shipmentTypeDecoder: Decoder[ShipmentTypeSQL] = decoder[ShipmentTypeSQL] {
    (index: Index, row: ResultRow, _: Session) =>
      ShipmentTypeSQL.decode(row.getString(index))
  }

  override def create(order: Order): IO[OrderError.ProductsNotFound , OrderId] = {

    val insertPaymentAddress =
      run {
        quote {
          addresses.insertValue(lift(AddressSQL.fromDomain(order.paymentAddress)))
        }
      }.orDie

    val insertShipmentAddress = ZIO
      .whenCase(order.shipmentAddress)({ case Some(shipmentAddress) =>
        run {
          quote {
            addresses.insertValue(lift(AddressSQL.fromDomain(shipmentAddress)))
          }
        }
      })
      .orDie

    val insertOrder = {
      val orderSQL = OrderSQL.fromDomain(order)
      run {
        quote {
          orders.insertValue(lift(orderSQL))
        }
      }.orDie
    }

    val insertOrderDetails = {
      val details = order.details.map(_.productId.value)
      run(quote(products.filter(product => liftQuery(details).contains(product.productId)))).orDie.flatMap {
        foundProducts =>
          val productIdsFromOrder = order.details.map(_.productId)
          val foundProductIds     = foundProducts.map(_.productId.toId)

          productIdsFromOrder.diff(foundProductIds) match {
            case head :: tail => ZIO.fail(OrderError.ProductsNotFound(head, tail))
            case Nil =>
              ZIO.foreach(order.details.map(OrderDetailSQL.fromDomain))(orderProductSQL =>
                run(quote(ordersDetails.insertValue(lift(orderProductSQL)))).orDie
              )
          }
      }
    }

    transaction {
      (insertPaymentAddress *> insertShipmentAddress *> insertOrder *> insertOrderDetails).either
    }.orDie.flatMap {
      case Left(error) => ZIO.fail(error)
      case Right(_)    => ZIO.succeed(order.orderId)
    }
  }

  override def getAll(offset: Int, limit: Int): UIO[List[Order]] =
    transaction {
      val data: ZIO[Any, SQLException, List[((OrderSQL, AddressSQL), Option[AddressSQL])]] = run {
        quote {
          ordersOffsetAndLimit(offset, limit)
            .join(addresses)
            .on({ case (order, paymentAddress) => order.paymentAddressId == paymentAddress.addressId })
            .leftJoin(addresses)
            .on({ case ((order, _), shipmentAddress) =>
              order.shipmentAddressId.contains(shipmentAddress.addressId)
            })
        }
      }

      data
        .flatMap(list =>
          ZIO.foreachPar(list) { case ((orderSQL, paymentAddressSQL), shipmentAddressSQLOpt) =>
            run(quote(ordersDetails.filter(_.orderId == lift(orderSQL.orderId)))).map(details =>
              orderSQL.toDomain(paymentAddressSQL, shipmentAddressSQLOpt, details).unsafeValidation
            )
          }
        )
    }.orDie

  override def getById(orderId: OrderId): IO[OrderError.NotFound, Order] =
    transaction {
      val result = for {
        orderOpt <- run(quote(orders.filter(_.orderId == lift(orderId.value)))).map(_.headOption).orDie
        order <- orderOpt match {
          case Some(value) => ZIO.succeed(value)
          case None        => ZIO.fail(OrderError.NotFound(orderId))
        }
        paymentAddressOpt <- run(quote(addresses.filter(_.addressId == lift(order.paymentAddressId))))
          .map(_.headOption)
          .orDie
        paymentAddress <- getOrDieWithMessage(
          paymentAddressOpt,
          errorMessageTemplate("payment address", order.paymentAddressId, "order", order.orderId)
        )

        shipmentAddressOpt <- order.shipmentAddressId match {
          case Some(shipmentAddressId) =>
            run(quote(addresses.filter(_.addressId == lift(shipmentAddressId)))).map(_.headOption).orDie.flatMap {
              case Some(foundAddress) => ZIO.succeed(Option(foundAddress))
              case None =>
                ZIO.dieMessage(errorMessageTemplate("shipment address", shipmentAddressId, "order", order.orderId))
            }
          case None => ZIO.none
        }
        orderDetails <- run(quote(ordersDetails.filter(_.orderId == lift(order.orderId)))).orDie

      } yield order.toDomain(paymentAddress, shipmentAddressOpt, orderDetails).unsafeValidation
      result.either
    }.orDie.flatMap {
      case Left(error)  => ZIO.fail(error)
      case Right(order) => ZIO.succeed(order)
    }

  override def updateStatus(orderId: OrderId, orderStatus: OrderStatus): IO[OrderError, Unit] = {

    val fetchOrder = quote(orders.filter(_.orderId == lift(orderId.value)))
    val result     = run(quote(fetchOrder)).orDie

    transaction {
      result.flatMap { returnedOrders =>
        returnedOrders.headOption match {
          case Some(order) if order.status.toDomain.canBeReplacedBy(orderStatus) =>
            val orderStatusSql = OrderStatusSQL.fromDomain(orderStatus)
            run(quote(fetchOrder.update(_.status -> lift(orderStatusSql)))).orDie
          case Some(_) => ZIO.fail(OrderError.InvalidStatus(orderId, orderStatus))
          case None    => ZIO.fail(OrderError.NotFound(orderId))
        }
      }.either
    }.orDie.flatMap {
      case Left(orderError) => ZIO.fail(orderError)
      case Right(_)         => ZIO.unit
    }
  }

  private def getOrDieWithMessage[A](option: Option[A], message: String): ZIO[Any, Nothing, A] =
    option match {
      case Some(value) => ZIO.succeed(value)
      case None        => ZIO.dieMessage(message)
    }

  private val errorMessageTemplate = (field: String, fieldId: UUID, entity: String, entityId: UUID) =>
    s"$field with id:$fieldId was not found for $entity with id:$entityId"
}

object OrderRepositoryPostgres {
  val live: URLayer[Quill.Postgres[CamelCase], OrderRepositoryPostgres] =
    ZLayer.fromFunction(OrderRepositoryPostgres.apply _)
}
