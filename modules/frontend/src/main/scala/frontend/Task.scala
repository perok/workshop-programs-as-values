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

class Task(world: World):
  def introduction(log: Log[IO]) =
    val numberPattern = ".*Please call ([0-9]+)\\..*".r

    for {
      _ <- Helpers.ignore("Done")
      number <- world.getPhone
        .flatMap(phone => phone.call("81549300"))
        .use(channel =>
          channel.say("Good day") >>
            channel.listen.take.flatMap(channel.say(_)) >>
            channel.listen.take.flatMap {
              case numberPattern(ma) =>
                log.info(ma).as(ma)
              case _ =>
                log.info("Match failed").as("")
            }
        )
      secret <- world.getPhone
        .flatMap(phone => phone.call(number))
        .use(channel => channel.say("Good day") >> Helpers.takeItAll(channel.listen).map(_.last))

    } yield secret

  def task1(log: Log[IO]) =
    Helpers.ignore("Not Yet")

  // Error handling
  def task2(log: Log[IO]) =
    /* Test ideas:
     *
     *
     *
     * Call 100 people in range 7000-8000 at the same time and get the
     * total sum they can give
     *   -> traverse vs parTraverse
     *
     *
     *
     * */
    List
      // .range(49999, 51001)
      .range(49999, 50111)
      .parTraverse { number =>
        world.getPhone.flatMap(_.call(number.toString)).use(_.say("How old are you?"))
      }
      .as("42")

  def task3(log: Log[IO]) =
    Helpers.ignore("Not Yet") >>
      (world.getPhone, world.getPhone).tupled.use { (phone1, phone2) =>
        for {
          _ <- (phone1.call("123478").use(_ => IO.sleep(3.seconds)).attempt,
                phone2.call("82326434").use(channel => channel.say("Hey") >> IO.sleep(5.seconds))
          ).parTupled
        } yield ""
      }

object Helpers:
  def takeItAll(in: QueueSource[IO, String]): IO[List[String]] =
    Stream.repeatEval(in.tryTake).unNoneTerminate.compile.toList

  def ignore(reason: String)(implicit pos: SourceLocation): IO[Nothing] =
    IO.raiseError(new IgnoredException(Some(reason), pos))
