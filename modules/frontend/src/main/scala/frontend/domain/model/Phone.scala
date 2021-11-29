package frontend.domain.model

import cats.*
import cats.effect.*
import cats.effect.std.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import fs2.Stream
import fs2.concurrent.*

import scala.concurrent.duration.*
import scala.util.control.NoStackTrace

/** Interact with the world
  */
trait World:

  /** Returns a phone that can be used within the scope of the [[cats.effect.Resource]].
    *
    * Main building block to solve the issues
    */
  def getPhone: Resource[IO, Phone]

  /** Only for internal use.
    */
  def worldState: SignallingRef[IO, Map[Unique.Token, PhoneState]]

/** Interact with phone
  */
trait Phone:
  /** Who do you want to contact? Give em a call!
    *
    * Can fail with [[frontend.domain.model.CallingEvent]] errors.
    *
    * {{{
    * val world: World = ???
    * val phoneCallingHome =
    *   world.getPhone
    *     .flatMap(phone => phone.call("12345"))
    *
    * phoneCallingHome.use(_.say("I'm home!")).handleErrorWith {
    *   case CallingEvent.NoAnswer => IO.println("Nobody home")
    * }
    * }}}
    */
  def call(number: String): Resource[IO, Person]

  /** Only for internal use.
    */
  def state: SignallingRef[IO, PhoneState]

// PlayerActions?
trait Person:
  // To communicate with the Person
  def say(something: String): IO[Unit]
  def listen: QueueSource[IO, String]

enum CallingEvent(desc: String) extends Exception(desc) with NoStackTrace:
  case Busy extends CallingEvent("Busy")
  case Hangup extends CallingEvent("Hangup")
  case InvalidNumber extends CallingEvent("InvalidNumber")
  case NoAnswer extends CallingEvent("Nobody answered")

enum WorldSpeed(speed: Int):
  case Instant extends WorldSpeed(0)
  case RealTime extends WorldSpeed(1)

//
// Internal details
//

trait PersonBotActions:
  self =>
  def willYouAnswer: IO[Boolean] = IO.pure(true)
  def onPhoneCallStarted(doHangup: IO[Unit], talk: QueueSink[IO, BotSaying]): IO[Unit] = IO.unit
  def onPhoneCallEnded: IO[Unit] = IO.unit
  def say(something: String, talk: QueueSink[IO, BotSaying], doHangup: IO[Unit]): IO[Unit]

  val info: PersonInfo =
    PersonInfo(
      "Ola Nordmann",
      "-1",
      "https://yt3.ggpht.com/ytc/AKedOLSRSl8xsTNuQU_f6sg3bHI19gZYUSqLu2I78S90MQ=s900-c-k-c0x00ffffff-no-rj"
    )

  def mapInfo(mapper: PersonInfo => PersonInfo): PersonBotActions = new PersonBotActions {
    override def onPhoneCallStarted(doHangup: IO[Unit], talk: QueueSink[IO, BotSaying]): IO[Unit] =
      self.onPhoneCallStarted(doHangup, talk)
    override def onPhoneCallEnded: IO[Unit] = self.onPhoneCallEnded
    def say(something: String, talk: QueueSink[IO, BotSaying], doHangup: IO[Unit]): IO[Unit] =
      self.say(something, talk, doHangup)
    override val info: PersonInfo = mapper(self.info)
  }

enum BotSaying(val text: String):
  case Text(override val text: String) extends BotSaying(text)
  case TextJustShow(override val text: String) extends BotSaying(text)
  case TextWithConfirmation(override val text: String, confirm: Deferred[IO, Unit])
      extends BotSaying(text)

final case class PersonInfo(name: String, number: String, image: String)

enum PhoneState:
  case Available
  case Dialing(number: String)
  case Calling(number: String)
  case Talking(person: PersonInfo, botSaying: Option[BotSaying], playerSaying: Option[String])
  case Hangup
