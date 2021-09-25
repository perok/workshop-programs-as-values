package no.perok.toucan.infrastructure.endpoint

import java.time.Instant

import cats.syntax.either._
import cats.syntax.option._
import io.circe.parser.decode
import io.circe.syntax._
import no.perok.toucan.config.Config
import no.perok.toucan.domain.models.{User, WithId}
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}

class AuthenticationLogic(settings: Config) {
  private val algorithm = JwtAlgorithm.HS256

  def makeToken(user: WithId[User], issued: Instant): String = {
    // TODO simpleJWTUser, or something
    val key = settings.tokenSecret

    val claim = JwtClaim(
      content = user.asJson.noSpaces,
      expiration = issued.plusSeconds(157784760).getEpochSecond.some,
      issuedAt = issued.getEpochSecond.some
    )

    JwtCirce.encode(claim, key, algorithm)
  }

  def authenticate(token: String): Either[String, WithId[User]] = {
    JwtCirce
      .decode(token, settings.tokenSecret, Seq(JwtAlgorithm.HS256))
      .toEither
      .leftMap(_.toString)
      .flatMap { claim =>
        implicit val clock: java.time.Clock =
          java.time.Clock.systemDefaultZone()

        Either.cond(claim.isValid, claim, "Claim no longer valid")
      }
      .flatMap { claim =>
        decode[WithId[User]](claim.content)
          .leftMap(x => s"Decoding user from claim content: ${x.toString}\n${claim.content}")
      }
  }
}
