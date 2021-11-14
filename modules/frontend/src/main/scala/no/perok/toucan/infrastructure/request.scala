package no.perok.toucan.infrastructure

import cats.effect.*
import sttp.client3.*
import sttp.capabilities.WebSockets
import sttp.tapir.client.sttp.SttpClientInterpreter

class Requests[F[_]](backend: SttpBackend[F, WebSockets]):
  val tapirInterpreter = SttpClientInterpreter()

  def performTapir[I, E, O, R](
      e: sttp.tapir.PublicEndpoint[I, E, O, Any]
  ): I => F[sttp.client3.Response[Either[E, O]]] = { input =>
    val req = tapirInterpreter
      .toRequestThrowDecodeFailures(e, None)
      .apply(input)

    backend.send(req)
  }

  def performTapirInfallible[I, O, R](
      e: sttp.tapir.PublicEndpoint[I, Nothing, O, Any]
  ): I => F[sttp.client3.Response[O]] = { input =>

    val req = tapirInterpreter
      .toRequestThrowErrors(e, None)
      .apply(input)

    backend.send(req)
  }

object Requests:
  import sttp.client3.impl.cats.FetchCatsBackend

  def apply[F[_]: Async]: Requests[F] =
    val backend = FetchCatsBackend[F]()
    new Requests(backend)
