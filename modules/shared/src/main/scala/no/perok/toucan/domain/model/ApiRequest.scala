package no.perok.toucan.domain.model

import io.circe._
import io.circe.derivation._
import cats.effect._
import java.time.Instant

import no.perok.toucan.domain.model.Requests.GetMovies

// Machinery to handle requests - RPC library
// TODO4Future - Will it be able to support a stream of results?
// TODO4Future - make Requests higher kinded? So it works like a library
trait ApiRequest[Request] {
  type Response
  val requestEncoder: Encoder[Request]
  val responseDecoder: Decoder[Response]
}
object ApiRequest {
  type Aux[Req, Res] = ApiRequest[Req] { type Response = Res }
  def apply[Request, Response_](
      implicit _requestEncoder: Encoder[Request],
      _responseDecoder: Decoder[Response_]
  ): ApiRequest.Aux[Request, Response_] = new ApiRequest[Request] {
    override type Response = Response_
    override val requestEncoder: Encoder[Request] = _requestEncoder
    override val responseDecoder: Decoder[Response_] = _responseDecoder
  }
}

// My requests
sealed trait Requests[R]
object Requests {
  // TODO convert to Codec for ApiRequests
  case class GetMovies(user: Int, instant: Instant) extends Requests[List[Instant]]

  object GetMovies {
    implicit val encoder: Encoder[GetMovies] = deriveEncoder[GetMovies]
    implicit val getMoviesApi: ApiRequest.Aux[GetMovies, List[Instant]] =
      ApiRequest[GetMovies, List[Instant]]
  }

  implicit def encoder[Req: Encoder]: Encoder[Requests[Req]] =
    // TODO get classname of req and use that as a key?
    // TODO make into a library . aka extends .. fixes this
    ???
}

object test {

  // Client
  val clientToBackendRequest: Requests.GetMovies = Requests.GetMovies(2, Instant.now())

  def sendRequest[RR, R <: Requests[RR]](req: R)(implicit api: ApiRequest.Aux[R, RR]): IO[RR] = {
    import io.circe.syntax._
    val requestString = req.asJson(api.requestEncoder).noSpaces
    val responseString = requestString

    IO.fromEither(io.circe.parser.decode[RR](responseString)(api.responseDecoder))
  }

  // Step 1, send the request
  val test: IO[List[Instant]] = sendRequest[List[Instant], GetMovies](clientToBackendRequest)

  // backend

  def performBackendBusiness[RR, R <: Requests[RR]](req: R): IO[RR] =
    req match {
      case Requests.GetMovies(_, _) =>
        IO.pure(List(Instant.now))
    }

  //
  // Her er neste utfordring
  // - må kanskje og konverte til codec for å få det løst
  //

  // TODO er denne mulig i det hele tatt?
  // shapeless GetMoveies :: Nil så kan man hente alle ApiRequsts for hver utgave.
  // Kan man få til det på en måte? Med Shapeless? Eller med en ny type class?
//  def backendReceiveAndDo[R <: Requests[_]](
//      in: String,
//      doit: Requests[_] => IO[_] // Maskineriet her må ikke forholde seg til doIt sine typer??
//  )(implicit api: ApiRequest.Aux[R, RR]): IO[Unit] = { ??? }
//    import io.circe.syntax._
//    import io.circe.parser._
//
////    decode[ToBackend](in)
//    // TODO ToBackend to ToResponse? Request with Response information
//
//    import req._
//
//    doit(req).map(a => a.asJson).as(())
//    ???
//  }

//  val received: Requests[_] = clientToBackendRequest
////  backendReceiveAndDo("", received, performBackendBusiness).as(()).unsafeRunSync()
//  performBackendBusiness(received).as(()).unsafeRunSync()

}
