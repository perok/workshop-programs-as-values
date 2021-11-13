package no.perok.toucan.util

import cats.effect.*

object requests:
  import sttp.tapir.client.sttp.SttpClientInterpreter
  import sttp.client3.impl.cats.FetchCatsBackend

  val tapirInterpreter = SttpClientInterpreter()
  val backend = FetchCatsBackend[IO]()

  def performTapir[I, E, O, R](
      e: sttp.tapir.PublicEndpoint[I, E, O, Any]
  ): I => IO[sttp.client3.Response[Either[E, O]]] = { input =>
    val req = tapirInterpreter
      .toRequestThrowDecodeFailures(e, None)
      .apply(input)

    backend.send(req)
  }

  def performTapirInfallible[I, O, R](
      e: sttp.tapir.PublicEndpoint[I, Nothing, O, Any]
  ): I => IO[sttp.client3.Response[O]] = { input =>

    val req = tapirInterpreter
      .toRequestThrowErrors(e, None)
      .apply(input)

    backend.send(req)
  }
