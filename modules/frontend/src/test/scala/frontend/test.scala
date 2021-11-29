package frontend

import cats.*
import cats.effect.*
import cats.effect.std.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import fs2.Stream
import fs2.concurrent.*
import weaver.*

import frontend.domain.model.*
import frontend.infrastructure.WorldImpl

object Tests extends SimpleIOSuite:
  given Eq[java.lang.Throwable] = Eq.fromUniversalEquals
  given Show[java.lang.Throwable] = Show.show(err => pprint.tokenize(err).mkString)

  test("Can call a simple person") {
    val t = for {
      world <- WorldImpl(PhoneBook.create(PhoneBook.simplePerson(_.copy(number = "82326434"))))
      phone <- world.getPhone
      // Calling unknown person fails
      _ <- phone
        .call("12345")
        .use_
        .attempt
        .flatMap(response => expect.eql(Left(CallingEvent.InvalidNumber), response).failFast[IO])
        .toResource

      // Calling known person works
      _ <- phone
        .call("82326434")
        .use(channel => channel.say("Hey") >> channel.listen.take)
        .flatMap(response => expect.eql("Hey", response).failFast)
        .toResource

      // Leaking person and try to talk fails
      _ <- phone
        .call("82326434")
        .use(channel => IO.pure(channel))
        .flatMap(_.say("Good day"))
        .attempt
        .flatMap(response => expect.eql(Left(CallingEvent.Hangup), response).failFast[IO])
        .toResource

      // Trying to call same person twice
      _ <- (phone.call("82326434"), phone.call("82326434")).parTupled.use_.attempt
        .flatMap(response => expect.eql(Left(CallingEvent.Busy), response).failFast[IO])
        .toResource
    } yield success

    t.use_.as(success)
  }

  // TODO use IO locals for speed settings!
  loggedTest("Can call a parrot") { log =>
    val phoneBook = PhoneBook.parrot(_.copy(number = "83743828"))

    val t = for {
      world <- WorldImpl(PhoneBook.create(phoneBook))
      phone <- world.getPhone
      endSignal <- SignallingRef.of(0).toResource

      ops = (iteration: String) =>
        phone
          .call("83743828")
          .use { channel =>
            val words = List("Hello", "world")

            words
              .traverse(w => channel.say(w) >> channel.listen.take)
              .flatMap(result => expect.eql(words, result).failFast)

          // channel.listen
          //   .concurrently(Stream.emits(words).evalMap(channel.say(_)))
          //   .interruptWhen(endSignal.discrete.map(_ == words.length))
          //   .evalTap(received => log.info(s"$iteration $received") >> endSignal.update(_ + 1))
          //   .compile
          //   .string
          //   .flatMap(result => expect.eql(words.mkString, result).failFast)
          }
      _ <- ops("first").toResource
      _ <- endSignal.set(0).toResource
      _ <- ops("second").toResource
      // TODO test were waiting for one word. Map to Option, then collectFirst
      // => None and unNoneTerminate
    } yield success

    t.use_.as(success)
  }

  loggedTest("Can call a grumpy person") { log =>
    val t = for {
      phoneBook <- PhoneBook.hangupPerson(3, _.copy(number = "82326434")).toResource
      world <- WorldImpl(PhoneBook.create(phoneBook))

      phone <- world.getPhone
      endSignal <- SignallingRef.of(0).toResource

      call = phone
        .call("82326434")
        .evalTap(_ => log.info("Hello"))
        .use(_.say("Goodday"))

      callExpectHangup =
        call.attempt
          .flatMap(response => expect.eql(Left(CallingEvent.Hangup), response).failFast[IO])
          .toResource

      callExpectSuccess =
        call.attempt
          .flatMap(response => expect.eql(Right(()), response).failFast[IO])
          .toResource

      _ <- (callExpectHangup.replicateA(3) >> callExpectSuccess)
    } yield success

    t.use_.as(success)
  }
