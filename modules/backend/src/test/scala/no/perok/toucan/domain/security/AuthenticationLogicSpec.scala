package no.perok.toucan.domain.security

import java.time.{Instant, LocalDateTime}

import no.perok.toucan.config.{Config, TestSettings}
import no.perok.toucan.domain.models._
import no.perok.toucan.infrastructure.endpoint.AuthenticationLogic
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AuthenticationLogicSpec extends AnyFlatSpec with Matchers {

  val secret = "muhahah"
  val user = WithId(ID[User](42), User("", "", LocalDateTime.now()))
  val settings: Config = TestSettings().copy(tokenSecret = secret)
  val jwtDomain = new AuthenticationLogic(settings)

  "The Jwt token" should "function.." in {
    val token = jwtDomain.makeToken(user, Instant.now)
    token should not be null
    jwtDomain.authenticate(token).right.get.id shouldEqual user.id
  }

  "The Jwt token" should "not authenticate on garbage in" in {
    val token = "THIS IS BULLSHIT"
    jwtDomain.authenticate(token).left.get.length should be > 0
  }
}
