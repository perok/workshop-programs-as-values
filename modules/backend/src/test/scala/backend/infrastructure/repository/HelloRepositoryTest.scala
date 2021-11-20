package backend.infrastructure.repository

import cats.effect.*
import skunk.Session
import weaver.*

import backend.config.*
import backend.infrastructure.repository.*
import backend.infrastructure.utils.DatabaseHelper

object TestDb extends IOSuite:
  override type Res = Session[IO]
  override def sharedResource: Resource[IO, Session[IO]] =
    DatabaseHelper.postgres

  test("Can say hello") { session =>
    for {
      helloResponse <- session.option(HelloRepository.getHello)
    } yield expect(helloResponse.isEmpty)
  }
