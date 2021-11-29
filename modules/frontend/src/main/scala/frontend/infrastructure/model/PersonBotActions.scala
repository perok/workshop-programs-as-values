package frontend.infrastructure.model

import cats.*
import cats.effect.*
import cats.effect.std.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import fs2.Stream
import fs2.concurrent.*

import frontend.domain.model.*

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
