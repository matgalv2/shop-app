package io.github.g4lowy.http.cyclicjobs

import io.github.g4lowy.order.domain.repository.OrderRepository
import zio.{Schedule, ZIO, durationInt}

object OrderJobs {

  def archiveOrdersOnceADay: ZIO[OrderRepository, Nothing, Unit] = {

    val interval = Schedule.fixed(1.day)

    OrderRepository.archiveDeliveredOrders
      .tap(ordersNumber => ZIO.log(s"Archived $ordersNumber orders"))
      .repeat(interval)
      .unit
  }
}
