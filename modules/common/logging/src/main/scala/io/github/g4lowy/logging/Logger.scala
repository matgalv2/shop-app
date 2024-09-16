package io.github.g4lowy.logging

import zio.{Has, Tag, UIO, ULayer, URIO, ZIO, ZLayer}

import java.time.LocalDateTime
import scala.io.AnsiColor._

trait Logger[-A] {

  def log(level: LogLevel)(line: => A): UIO[Unit]
  def info(line: => A): UIO[Unit]  = log(LogLevel.Info)(line)
  def warn(line: => A): UIO[Unit]  = log(LogLevel.Warn)(line)
  def error(line: => A): UIO[Unit] = log(LogLevel.Error)(line)
}

object Logger {
  def log[A: Tag](level: LogLevel)(line: => A): URIO[Has[Logger[A]], Unit] =
    ZIO.serviceWith[Logger[A]](_.log(level)(line))

  def info[A: Tag](line: => A): URIO[Has[Logger[A]], Unit] =
    ZIO.serviceWith[Logger[A]](_.info(line))

  def warn[A: Tag](line: => A): URIO[Has[Logger[A]], Unit] =
    ZIO.serviceWith[Logger[A]](_.warn(line))

  def error[A: Tag](line: => A): URIO[Has[Logger[A]], Unit] =
    ZIO.serviceWith[Logger[A]](_.error(line))

  implicit val basicLogger: Logger[String] = new Logger[String] {
    override def log(level: LogLevel)(line: => String): UIO[Unit] = {
      val colour = level match {
        case LogLevel.Info  => ""
        case LogLevel.Warn  => YELLOW
        case LogLevel.Error => RED
      }
      ZIO.succeed(println(f"$colour${LocalDateTime.now()} ${level.toString.toUpperCase} $line$RESET"))
    }
  }

  val layer: ULayer[Has[Logger[String]]] =
    ZLayer.succeed(basicLogger)
}
