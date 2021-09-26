package no.perok.toucan.infrastructure.endpoint

import java.time.Instant

import cats.data._
import cats.syntax.all._
import cats.effect._
import io.circe.syntax.EncoderOps
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware
import no.perok.toucan.config.Config
import no.perok.toucan.domain.algebras.UserAlgebra
import no.perok.toucan.domain.models._
import no.perok.toucan.infrastructure.CryptographyLogic

class AuthenticationEndpoint[F[_]: Async](userAlgebra: UserAlgebra[F], settings: Config)
    extends Http4sDsl[F] {
  val tokenHandler = new AuthenticationLogic(settings)

  def verifyLogin(request: Request[F]): F[Either[String, WithId[User]]] = {
    val credentialFlow: EitherT[F, String, WithId[User]] = for {
      // TODO change to basicCredentials Authentication header
      // credentials <- EitherT.right(request.asJsonDecode[BasicCredentials])
      credentials <- EitherT.pure[F, String](BasicCredentials("", ""))
      userHashedPW <- EitherT.right(userAlgebra.getUserHashByUsername(credentials.username))
      username <- EitherT.fromEither[F](userHashedPW match {
        case Some(hash) if CryptographyLogic.verifyPassword(credentials.password, hash) =>
          credentials.username.asRight
        case _ =>
          "Username or password incorrect".asLeft
      })
      user <- EitherT(
        userAlgebra
          .getUserByUsername(username)
          .map(_.toRight("No User found, which makes no sense"))
      ) // TODO fatal error
    } yield user

    credentialFlow.value
  }

  val retrieveUser: Kleisli[F, ID[User], Option[WithId[User]]] = Kleisli({ id =>
    OptionT(userAlgebra.getUserById(id)).map(WithId(id, _)).value
  })

  val logIn: Kleisli[F, Request[F], Response[F]] = Kleisli({ request =>
    verifyLogin(request: Request[F]).flatMap {
      case Left(error) =>
        Forbidden(error)
      case Right(user) =>
        val message = tokenHandler.makeToken(user, Instant.now)

        Ok("Logged in!").map(_.addCookie("authcookie", message))
    }
  })

  protected val authUser: Kleisli[F, Request[F], Either[String, WithId[User]]] =
    Kleisli({ request =>
      val message = for {
        header <-
          request.headers
            .get[headers.Cookie]
            .toRight("Cookie parsing error")
        cookie <- header.values.toList
          .find(_.name === "authcookie")
          .toRight("Couldn't find the authcookie")
        jwtUser <- tokenHandler
          .authenticate(cookie.content)
        message <- Either
          .catchOnly[NumberFormatException](jwtUser.id)
          .leftMap(_.toString)
      } yield message

      message
        .traverse(retrieveUser.run)
        .map(_.flatMap(_.toRight("No User")))
    })

  private val onFailure: Kleisli[OptionT[F, *], AuthedRequest[F, String], Response[F]] = ???
  //Kleisli(req => OptionT.liftF(Forbidden(req.authInfo)))

  private val errorHandler: PartialFunction[Throwable, F[Response[F]]] = { case unknown @ _ =>
    InternalServerError(show"Unknown error: ${unknown.getMessage()}")
  }

  // To force authentication on services
  // TODO skift til BasicAuthenticatorm
  // https://github.com/http4s/http4s/blob/master/examples/src/main/scala/com/example/http4s/ExampleService.scala
  // den web tjesten?
  // TSec
  val middleware: AuthMiddleware[F, WithId[User]] = AuthMiddleware(authUser, onFailure)

  // To authenticate users
  def authenticationHttp: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root / "login" =>
        logIn.run(req)
      case req @ POST -> Root / "user" =>
        val res = for {
          newUser <- EitherT.liftF(req.decodeJson[NewUserForm])
          newId <- EitherT(userAlgebra.addUser(newUser))
        } yield newId.asJson

        res.value
          .flatMap {
            case Left(a) => Conflict(a)
            case Right(a) => Ok(a)
          }
          .handleErrorWith(errorHandler)
      // http://localhost:8080/api/auth/auth0/callback#access_token=OyZbUfq28clbgm5UbmWB2ku_1J7sVOOr&expires_in=7200&token_type=Bearer&state=XQ4op2Cz5pvKKg512GjB~N5VcmcikfVH&id_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Ik1EVkdRMEZHUXpNMFJEQkVOREZGUVRaQ1JURkVPVUl4T0RkRk5UazBOekZFTjBJMk9VVkZSUSJ9.eyJpc3MiOiJodHRwczovL3Blcm9rLmV1LmF1dGgwLmNvbS8iLCJzdWIiOiJnb29nbGUtb2F1dGgyfDExNjQ4MDA1NzIxMTY0MjExMzA2MCIsImF1ZCI6ImM4b1Jtd2ZiVmhIbHBvY05nVUFwZ1BVOGJwdDV3V2Z0IiwiaWF0IjoxNTM5NTIzODM0LCJleHAiOjE1Mzk1NTk4MzQsImF0X2hhc2giOiI0dTBXMUFBNzlVUWpEbV8xVlEwZlR3Iiwibm9uY2UiOiIxcndQNTFyaW44dU5ZR0pQQy0wdWZla2ZkNkRLcnRuayJ9.pnEp53X8-afVnq2lRMbdx6F2NGr4-Jc5z9SAYY-LT9ZnquqAbOlUgvOf91T5rr0d2IqZ8I_lPzTGpt4wEkeaVDtfV6-XYASfxUdJsrF4Q46WkjJVSf_5ADQPxP5xMI5KP99wD2A6weONOTISgr-vaoLGj8ZO_DgYEVbTBdVjgLdP_0vj3C3a8dOjGP_ZQ1q06HGaAnhbBwBCcbPA4rFXaAMxThEWXgr8IlvxxyaYAm_LoaRzDmHTpdVMcWhxDj9NoD7yupBN8BSNWf5ZBJEopPvf8dTCjDl2jvL9ouIjL0haPpNQw7sY84GIXMJN2z2LeKvm6Lc5DM-pz_ryq7lcjg
    }
}
