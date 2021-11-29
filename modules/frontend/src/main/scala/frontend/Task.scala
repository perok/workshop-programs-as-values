package frontend

import cats.*
import cats.effect.*
import cats.effect.std.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import fs2.Stream
import weaver.*

import scala.concurrent.duration.*
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.util.matching.Regex

import frontend.domain.model.*

/** Define expectations with https://disneystreaming.github.io/weaver-test/docs/expectations
  */
class Task(world: World) extends SimpleIOSuite:
  def introduction(log: Log[IO]) =
    ignore("Not Yet")

  def task1(log: Log[IO]) =
    ignore("Not Yet")

  def task2(log: Log[IO]) =
    ignore("Not Yet")

  def task3(log: Log[IO]) =
    ignore("Not Yet")

object Helpers:
  def takeItAll(in: QueueSource[IO, String]): IO[List[String]] =
    Stream.repeatEval(in.tryTake).unNoneTerminate.compile.toList
