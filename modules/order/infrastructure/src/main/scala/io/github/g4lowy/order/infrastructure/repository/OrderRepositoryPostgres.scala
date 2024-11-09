package io.github.g4lowy.order.infrastructure.repository

import io.getquill.{ CamelCase, EntityQuery, QAC }
import io.getquill.jdbczio.Quill
import io.github.g4lowy.customer.infrastructure.model.CustomerSQL
import io.github.g4lowy.order.domain.model.{ Order, OrderError, OrderId, OrderStatus }
import io.github.g4lowy.order.domain.repository.OrderRepository
import io.github.g4lowy.order.infrastructure.model.{
  AddressSQL,
  OrderDetailSQL,
  OrderSQL,
  OrderStatusSQL,
  PaymentTypeSQL,
  ShipmentTypeSQL
}
import org.postgresql.util.PGobject
import zio.{ IO, UIO, URIO, ZIO }

import java.sql.{ SQLException, Types }
import java.util.UUID

case class OrderRepositoryPostgres(quill: Quill.Postgres[CamelCase]) extends OrderRepository {

  import quill._

  private val orders = quote {
    querySchema[OrderSQL]("Orders")
  }

  private val addresses = quote {
    querySchema[AddressSQL]("Addresses")
  }

  private val ordersDetails = quote {
    querySchema[OrderDetailSQL]("OrdersDetails")
  }

  private val customers = quote {
    querySchema[CustomerSQL]("Customers")
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

  override def create(order: Order): UIO[OrderId] = {

    val insertPaymentAddress =
      run {
        quote {
          addresses.insertValue(lift(AddressSQL.fromDomain(order.paymentAddress)))
        }
      }

    val insertShipmentAddress = ZIO.whenCase(order.shipmentAddress)({ case Some(shipmentAddress) =>
      run {
        quote {
          addresses.insertValue(lift(AddressSQL.fromDomain(shipmentAddress)))
        }
      }
    })

    val insertOrderProducts =
      ZIO.foreach(order.details.map(OrderDetailSQL.fromDomain))(orderProductSQL =>
        run(quote(ordersDetails.insertValue(lift(orderProductSQL))))
      )

    val insertOrder =
      run {
        quote {
          orders.insertValue(lift(OrderSQL.fromDomain(order)))
        }
      }

    transaction(insertPaymentAddress *> insertShipmentAddress *> insertOrderProducts *> insertOrder)
      .as(order.orderId)
      .orDie
  }

  override def getAll: UIO[List[Order]] = transaction {
    val data: UIO[List[(OrderSQL, CustomerSQL, AddressSQL, Option[AddressSQL])]] = run {
      quote {
        for {
          order          <- orders
          customer       <- customers if customer.customerId == order.customerId
          paymentAddress <- addresses if paymentAddress.addressId == order.paymentAddressId
          shipmentAddress <- orders
            .leftJoin(addresses)
            .on((order, address) => order.shipmentAddressId.contains(address.addressId))
        } yield (order, customer, paymentAddress, shipmentAddress._2)
      }
    }.orDie

    data
      .flatMap(list =>
        ZIO.foreachPar(list) {
          case (
                orderSQL: OrderSQL,
                customerSQL: CustomerSQL,
                paymentAddressSQL: AddressSQL,
                shipmentAddressSQL: Option[AddressSQL]
              ) =>
            run(quote(ordersDetails.filter(_.orderId == orderSQL.orderId))).map(details =>
              orderSQL.toDomain(customerSQL, paymentAddressSQL, shipmentAddressSQL, details)
            )
        }
      )
  }.orDie

  override def getById(orderId: OrderId): IO[OrderError.NotFound, Order] =
    transaction {
      val result = for {
        orderOpt <- run(quote(orders.filter(_.orderId == orderId.value))).map(_.headOption).orDie
        order <- orderOpt match {
          case Some(value) => ZIO.succeed(value)
          case None        => ZIO.fail(OrderError.NotFound(orderId))
        }
        customerOpt <- run(quote(customers.filter(_.customerId == order.customerId))).map(_.headOption).orDie
        customer <- getOrDieWithMessage(
          customerOpt,
          errorMessageTemplate("Customer", order.customerId, "order", order.orderId)
        )
        paymentAddressOpt <- run(quote(addresses.filter(_.addressId == order.paymentAddressId))).map(_.headOption).orDie
        paymentAddress <- getOrDieWithMessage(
          paymentAddressOpt,
          errorMessageTemplate("payment address", order.paymentAddressId, "order", order.orderId)
        )

        shipmentAddressOpt <- order.shipmentAddressId match {
          case Some(shipmentAddressId) =>
            run(quote(addresses.filter(_.addressId == shipmentAddressId))).map(_.headOption).orDie.flatMap {
              case Some(foundAddress) => ZIO.succeed(Option(foundAddress))
              case None =>
                ZIO.dieMessage(errorMessageTemplate("shipment address", shipmentAddressId, "order", order.orderId))
            }
          case None => ZIO.none
        }
        orderDetails <- run(quote(ordersDetails.filter(orderDetail => orderDetail.orderId == order.orderId))).orDie

      } yield order.toDomain(customer, paymentAddress, shipmentAddressOpt, orderDetails)
      result.either
    }.orDie.flatMap {
      case Left(error)  => ZIO.fail(error)
      case Right(order) => ZIO.succeed(order)
    }

  override def updateStatus(orderId: OrderId, orderStatus: OrderStatus): IO[OrderError, Unit] = {

    val fetchOrder = orders.filter(_.orderId == orderId.value)
    val result     = run(quote(fetchOrder)).orDie

    transaction {
      result.flatMap { returnedOrders =>
        returnedOrders.headOption match {
          case Some(order) if order.status.toDomain.canBeReplacedBy(orderStatus) =>
            run(quote(fetchOrder.update(_.status -> OrderStatusSQL.fromDomain(orderStatus)))).orDie
          case Some(_) => ZIO.fail[OrderError](OrderError.InvalidStatus(orderId, orderStatus))
          case None    => ZIO.fail[OrderError](OrderError.NotFound(orderId))
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
