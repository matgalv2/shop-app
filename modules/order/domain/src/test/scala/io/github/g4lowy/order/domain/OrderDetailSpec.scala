package io.github.g4lowy.order.domain

import io.github.g4lowy.abstractType.Id.UUIDOps
import io.github.g4lowy.order.domain.model.{OrderDetail, OrderId}
import zio.Scope
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

import java.util.UUID

object OrderDetailSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("Order detail validation should")(
      test("fail when quantity is less than 1") {
        val orderDetail = OrderDetail.Unvalidated(OrderId.generate, UUID.randomUUID().toId, 0, BigDecimal(12))
        assertTrue(orderDetail.validate.isInvalid)
      },
      test("succeed when quantity is at least 1") {
        val orderDetail = OrderDetail.Unvalidated(OrderId.generate, UUID.randomUUID().toId, 1, BigDecimal(12))
        assertTrue(orderDetail.validate.isValid)
      },
      test("fail when price is less than 0.01") {
        val orderDetail = OrderDetail.Unvalidated(OrderId.generate, UUID.randomUUID().toId, 1, BigDecimal(0))
        assertTrue(orderDetail.validate.isInvalid)
      },
      test("succeed when price is at least 0.01") {
        val orderDetail = OrderDetail.Unvalidated(OrderId.generate, UUID.randomUUID().toId, 1, BigDecimal(0.01))
        assertTrue(orderDetail.validate.isValid)
      }
    )
}
