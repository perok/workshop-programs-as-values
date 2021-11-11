package no.perok.toucan

import cats.effect._
import cats.syntax.all._
import doobie.Transactor
import no.perok.toucan.config.{Config, DBConfig}
import no.perok.toucan.domain.TroopProgram
import no.perok.toucan.infrastructure.endpoint._
import no.perok.toucan.infrastructure.interpreter._
import org.http4s._
import org.http4s.client._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple {
  implicit def localLogger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  def run = program[IO].useForever

  def program[F[_]: Async]: Resource[F, Server] =
    for {
      settings <- Resource.eval(Config.config.load[F])
      xa <- Resource.eval(DBConfig.getXA(settings.db, dropFirst = false))

      _ <- Resource.eval(
        Logger[F].info(
          show"Server starting at http://localhost:${settings.server.port}/"
        )
      )

      client <- EmberClientBuilder.default[F].build

      result <- startServer[F](xa, client, settings)
    } yield result

  private def startServer[F[_]: Async](xa: Transactor[F],
                                       client: Client[F],
                                       settings: Config
  ): Resource[F, Server] = {
    //
    // DI
    //
    val voteInterpreter = new VoteInterpreter(xa)
    val troopInterpreter = new TroopInterpreter(xa)
    val userInterpreter = new UserInterpreter(xa)

    val handler = new TroopProgram(troopInterpreter, voteInterpreter)
    // TODO servce static resources: sttp.tapir.resourceServerEndpoint
    // TODO redoc documentation sttp.tapir.redoc.Redoc[IO]("")

    //
    // Service setup
    //
    val authenticationServices = new AuthenticationEndpoint(userInterpreter, settings)

    val apiServices: HttpRoutes[F] = HttpRoutes.empty

    val routes = Router(
      ("/api/auth", authenticationServices.authenticationHttp),
      ("/api", apiServices),
      ("/public", StaticEndpoint.endpoints)
    )

    EmberServerBuilder
      .default[F]
      .withPort(settings.server.port)
      .withHttpApp(routes.orNotFound)
      .build
  }

}
