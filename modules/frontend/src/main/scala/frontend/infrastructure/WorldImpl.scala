package frontend.infrastructure

import cats.*
import cats.effect.*
import cats.effect.std.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import fs2.Stream
import fs2.concurrent.*

import scala.concurrent.duration.*
import scala.scalajs.js.Math

import frontend.domain.model.*
import frontend.infrastructure.model.*

object WorldImpl:
  def apply(phoneBook: Map[String, PersonBotActions],
            worldSpeed: WorldSpeed = WorldSpeed.Instant
  ): Resource[IO, World] = for {
    given Random[IO] <- Random.scalaUtilRandom[IO].toResource
    worldSpeed <- IO.ref(worldSpeed).toResource
    worldState <- SignallingRef[IO].of(Map.empty[Unique.Token, PhoneState]).toResource
    dispatcher <- Dispatcher[IO]
  } yield new WorldImpl(worldSpeed, worldState, phoneBook, dispatcher)

class WorldImpl(speed: Ref[IO, WorldSpeed],
                worldState1: SignallingRef[IO, Map[Unique.Token, PhoneState]],
                phoneBook: Map[String, PersonBotActions],
                dispatcher: Dispatcher[IO]
)(
    using Random[IO]
) extends World:
  import PhoneState.*

  def worldState = worldState1

  def sleepStrict(to: FiniteDuration) =
    speed.get.flatMap {
      case WorldSpeed.Instant =>
        IO.unit
      case WorldSpeed.RealTime =>
        IO.sleep(to)
    }

  def sleepRandom(to: Int) =
    speed.get.flatMap {
      case WorldSpeed.Instant =>
        IO.unit
      case WorldSpeed.RealTime =>
        Random[IO].nextFloat
          .map(a => (a * to).toInt.seconds)
          .flatMap(IO.sleep)
    }

  def getPhone: Resource[IO, Phone] =
    for {
      uniqueId <- Unique[IO].unique.toResource
      mutex <- Semaphore[IO](1).toResource
      supervisor <- Supervisor[IO]
      currentState <- SignallingRef[IO].of(Available).toResource
      _ <-
        currentState.discrete // TODO SuperVisor or background or Topic or MapRef (if SignallingRef)
          .evalMap(newState => worldState.update(_.updated(uniqueId, newState)))
          .compile
          .drain
          .background
          .onFinalize(worldState.update(_.removed(uniqueId)))
    } yield new Phone {
      def state = currentState

      val doHangup =
        currentState.set(Hangup) >>
          sleepStrict(1.seconds) >>
          currentState.set(Available)

      // https://infusion.media/content-marketing/how-to-calculate-reading-time/
      def sleepTimeText(in: String) =
        Math.max((in.split(" ").length / 200.0).toInt * 60.0, 1).seconds

      def botSays(ths: Option[BotSaying]): IO[Unit] =
        currentState.modify {
          case a: PhoneState.Talking =>
            val waitOp = ths match {
              case Some(BotSaying.TextWithConfirmation(_, deferred)) => deferred.get
              case _ => sleepStrict(sleepTimeText(ths.map(_.text).orEmpty))
            }

            val setNewLastSaying =
              (waitOp >> botSays(None)).whenA(ths.isDefined)

            a.copy(botSaying = ths) -> setNewLastSaying
          case a =>
            a -> IO.unit
        }.flatten

      def playerSays(ths: Option[String]): IO[Unit] =
        currentState.modify {
          case a: PhoneState.Talking =>
            a.copy(playerSaying = ths) -> (sleepStrict(sleepTimeText(ths.orEmpty)) >>
              playerSays(None)).whenA(ths.isDefined)
          case a =>
            a -> IO.unit
        }.flatten

      trait Communicating:
        def hangup: IO[Unit]

      def tryCall(number: String) = currentState.modify {
        case Available =>
          val checkNumberExists = sleepStrict(2.seconds) >> phoneBook.get(number).match {
            case Some(existing) =>
              val wrappedPerson =
                (Deferred[IO, Either[Throwable, Unit]], Queue.bounded[IO, BotSaying](1)).mapN {
                  (deferredError, talkingQueue) =>
                    // Note: The .star on the `.on` calls. Should be done another
                    // way?
                    val failIfError =
                      deferredError.tryGet.flatMap(_.fold(IO.unit)(_.liftTo[IO]))
                    val failIfErrorK: IO ~> IO = new (IO ~> IO) {
                      def apply[A](a: IO[A]): IO[A] = failIfError >> a
                    }

                    val performHangup =
                      deferredError.complete(CallingEvent.Hangup.asLeft)
                        >> existing.onPhoneCallEnded >> doHangup

                    val noAnswer =
                      deferredError.complete(CallingEvent.NoAnswer.asLeft)
                        >> existing.onPhoneCallEnded >> doHangup

                    val mouth = new QueueSink[IO, BotSaying] {
                      def offer(a: BotSaying): IO[Unit] =
                        failIfErrorK(a match {
                          case _: BotSaying.Text =>
                            (talkingQueue.offer(a).start.void >> botSays(a.some))
                          case _: BotSaying.TextJustShow =>
                            botSays(a.some)
                          case _: BotSaying.TextWithConfirmation =>
                            botSays(a.some)
                        })
                      def tryOffer(a: BotSaying): IO[Boolean] =
                        failIfErrorK(a match {
                          case _: BotSaying.Text =>
                            // TODO perhaps update saying on false as well?,  but if queue is full then user is not listening. Then show red talk bubble?
                            talkingQueue
                              .tryOffer(a)
                              .ifM(botSays(a.some).as(true), IO.pure(false))
                          case _: BotSaying.TextJustShow =>
                            botSays(a.some).as(true)
                          case _: BotSaying.TextWithConfirmation =>
                            botSays(a.some).as(true)
                        })
                    }

                    val playerListen = new QueueSource[IO, String] {
                      val failOnlyIfEmpty = talkingQueue.size.map(_ === 0).ifM(failIfError, IO.unit)

                      def take: IO[String] = failOnlyIfEmpty >> talkingQueue.take.map(_.text)
                      def tryTake: IO[Option[String]] =
                        failOnlyIfEmpty >>
                          talkingQueue.tryTake.map(_.map(_.text))
                      def size: IO[Int] = failOnlyIfEmpty >> talkingQueue.size
                    }

                    val innerPerson = new PhoneInteraction with Communicating:
                      def hangup = performHangup
                      def say(something: String) =
                        failIfError >> playerSays(something.some) >>
                          existing.say(something, mouth, performHangup)
                      def listen = playerListen

                    existing.willYouAnswer.ifM(
                      currentState.set(Talking(existing.info, None, None)) >>
                        existing
                          .onPhoneCallStarted(performHangup, mouth)
                          .as(innerPerson),
                      noAnswer >> IO.raiseError(CallingEvent.NoAnswer)
                    )

                }

              sleepRandom(4) >> wrappedPerson.flatten

            case None =>
              doHangup >> IO.raiseError(CallingEvent.InvalidNumber)
          }

          Dialing(number) -> checkNumberExists
        case other =>
          other -> IO.raiseError(CallingEvent.Busy)
      }.flatten

      def call(number: String): Resource[IO, PhoneInteraction] =
        Resource.make(tryCall(number))(_.hangup)
    }
