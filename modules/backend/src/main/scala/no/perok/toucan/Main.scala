package no.perok.toucan

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import cats.syntax.all._
import cats.effect._
import fs2.Stream
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.blaze.server._
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client
import doobie.Transactor
import no.perok.toucan.config.{Config, DBConfig}
import no.perok.toucan.domain.TroopProgram
import no.perok.toucan.infrastructure.interpreter._
import no.perok.toucan.infrastructure.endpoint._

import scala.concurrent.ExecutionContext.Implicits.global
import cats.effect.{Resource, Temporal}

object Main extends IOApp {

  implicit def localLogger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  def run(args: List[String]): IO[ExitCode] = {
    program[IO].flatMap(_.compile.drain.as(ExitCode.Success))
  }

  // TODO Resource
  def program[F[_]: Async]: F[Stream[F, ExitCode]] =
    for {
      settings <- Config[F]
      _ <- Sync[F].fromEither(ensureEnv(settings))
      xa <- DBConfig.getXA(settings.db, dropFirst = false)
      _ <- Logger[F].info(show"Server starting at http://localhost:${settings.server.port}/")
    } yield (for {
      blocker <- Stream.resource(Resource.unit[F])
      client <- BlazeClientBuilder[F](global).stream // TODO use .resource
      result <- startServer[F](xa, client, settings)
    } yield result)

  /*
    setResourceBase
          Resource.newResource(ClassLoader.getSystemResource("public"))
          .getURI
.toASCIIString
   */

  private def startServer[F[_]: Async: Temporal](xa: Transactor[F],
                                                 client: Client[F],
                                                 settings: Config
  ): Stream[F, ExitCode] = {
    //
    // DI
    //
    val voteInterpreter = new VoteInterpreter(xa)
    val troopInterpreter = new TroopInterpreter(xa)
    val userInterpreter = new UserInterpreter(xa)

    val handler = new TroopProgram(troopInterpreter, voteInterpreter)

    //
    // Service setup
    //
    val authenticationServices = new AuthenticationEndpoint(userInterpreter, settings)

    val apiServices: HttpRoutes[F] = ???

    val routes = Router(
      ("/api/auth", authenticationServices.authenticationHttp),
      ("/api", apiServices),
      ("/public", StaticEndpoint.endpoints)
    ).orNotFound

    BlazeServerBuilder[F](global)
      .bindHttp(settings.server.port)
      .withHttpApp(routes)
      .serve
  }

  private def ensureEnv(settings: Config): Either[Throwable, Unit] =
    // If `SCALA_ENV` is defined then config must correspond
    // TODO reorder so if env.isProduction must then match ENV
    sys.env.get("SCALA_ENV") match {
      case Some(env) if env =!= settings.environment =>
        new RuntimeException(
          s"Incorrect environment: Running in $env, but config for ${settings.environment}"
        ).asLeft
      case _ => ().asRight
    }
}
