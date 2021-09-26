package no.perok.toucan.domain.security

import java.time.{Instant, LocalDateTime}
import munit._
import cats.syntax.all._

import no.perok.toucan.config.{Config, TestSettings}
import no.perok.toucan.domain.models._
import no.perok.toucan.infrastructure.endpoint.AuthenticationLogic

class AuthenticationLogicSpec extends FunSuite {

  val secret = "muhahah"
  val user = WithId(ID[User](42), User("", "", LocalDateTime.now()))
  val settings: Config = TestSettings().copy(tokenSecret = secret)
  val jwtDomain = new AuthenticationLogic(settings)

  test("The Jwt token should function..") {
    val token = jwtDomain.makeToken(user, Instant.now)
    assertEquals(token, null)
    assertEquals(jwtDomain.authenticate(token).map(_.id), Right(user.id))
  }

  test("The Jwt token should not authenticate on garbage in") {
    val token = "THIS IS BULLSHIT"
    assertEquals(jwtDomain.authenticate(token).leftMap(_.length > 0), Left(true))
  }
}
