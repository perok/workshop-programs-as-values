package backend

import cats.*
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import org.http4s.*
import org.http4s.client.*
import org.http4s.server.*
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import sttp.tapir.server.ServerEndpoint
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import skunk.Session
import natchez.Trace.Implicits.noop
import backend.infrastructure.interpreter.*
import backend.domain.*
import backend.config.*

object Main extends IOApp.Simple:
  implicit def localLogger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  def run = program[IO].useForever

  def program[F[_]: Async: Console]: Resource[F, Server] =
    for {
      settings <- Resource.eval(Config.load.load[F])
      _ <- Resource.eval(
        SchemaMigration
          .migrate[F](settings.db, dropFirst = true)
      )
      session <- Session.pooled[F](host = "localhost",
                                   user = settings.db.user,
                                   database = settings.db.name,
                                   password = Some(settings.db.password),
                                   max = 16
      )

      _ <- Resource.eval(
        Logger[F].info(
          show"Server starting at http://localhost:${settings.server.port}/"
        )
      )

      client <- EmberClientBuilder.default[F].build

      endpoints = setupDependencies(session, client, settings)

      result <- startServer[F](settings, endpoints)
    } yield result

  def setupDependencies[F[_]: MonadCancelThrow](session: Resource[F, Session[F]],
                                                client: Client[F],
                                                settings: Config
  ): List[ServerEndpoint[Any, F]] =
    import shared.api.ApiRequest

    val helloInterpreter = new HelloInterpreter[F](session)

    val helloProgram = new HelloProgram[F](helloInterpreter)

    List(ApiRequest.hello.serverLogicSuccess { _ =>
      helloProgram.getHello
    })

  def startServer[F[_]: Async](
      settings: Config,
      endpoints: List[ServerEndpoint[Any, F]]
  ): Resource[F, Server] =
    val httpRoutes = TapirConfiguration.routes(endpoints)

    val documentation = {
      import sttp.tapir.openapi.circe.yaml.*
      import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
      import sttp.tapir.redoc.Redoc

      val docsAsYaml =
        OpenAPIDocsInterpreter()
          .serverEndpointsToOpenAPI(endpoints, "Toucan", "1.0")
          .toYaml

      val documentationEndpoints = Redoc[F]("Toucan", docsAsYaml)

      TapirConfiguration.routes[F](documentationEndpoints)
    }

    val staticIndex = {
      import org.http4s.*
      import org.http4s.dsl.*
      object dsl extends Http4sDsl[F]
      import dsl.*

      HttpRoutes.of[F] { case request @ GET -> Root / "index.html" =>
        StaticFile
          .fromResource("index.html", Some(request))
          .getOrElseF(NotFound())
      }
    }
    import org.http4s.server.staticcontent.*
    val staticAssets = resourceServiceBuilder[F]("/assets").toRoutes

    EmberServerBuilder
      .default[F]
      .withPort(settings.server.port)
      .withHttpApp((httpRoutes <+> documentation <+> staticIndex <+> staticAssets).orNotFound)
      .build

object TapirConfiguration:
  import sttp.tapir.*
  import sttp.tapir.server.*
  import sttp.tapir.server.interceptor.*
  import sttp.tapir.server.interceptor.decodefailure.*

  def errorMessage[R](ctx: DecodeFailureContext): String =
    ctx.failure match {
      case DecodeResult.Error(_, circeErr: io.circe.Errors) =>
        circeErr.toList.map(_.show).mkString("\n")
      case DecodeResult.Error(_, circeErr: io.circe.Error) =>
        circeErr.show
      case _ =>
        DefaultDecodeFailureHandler.FailureMessages.failureMessage(ctx)
    }

  def decodeFailureHandler[F[_]]: DecodeFailureHandler =
    DefaultDecodeFailureHandler.default
      .copy(failureMessage = errorMessage)

  import org.http4s.*
  import sttp.capabilities.WebSockets
  import sttp.capabilities.fs2.Fs2Streams

  def routes[F[_]: Async](
      endpoints: List[ServerEndpoint[Fs2Streams[F], F]]
  ): HttpRoutes[F] =
    import sttp.tapir.server.http4s.*

    val specializedErrorHandler =
      Http4sServerOptions
        .customInterceptors[F, F]
        .decodeFailureHandler(decodeFailureHandler[F])
        .options

    Http4sServerInterpreter[F](specializedErrorHandler).toRoutes(endpoints)
