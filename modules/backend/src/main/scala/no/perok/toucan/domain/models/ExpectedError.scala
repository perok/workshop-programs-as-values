package no.perok.toucan.domain.models

/* ExpectedError Error for planned faults that can be shown to the user */
// TODO handle these at Tapir layer
sealed trait ExpectedError extends Throwable:
  val error: String

object ExpectedError:
  final case class Duplicate(error: String) extends ExpectedError
  object Unknown extends ExpectedError:
    val error: String = "Unknown"

  /* def errorToHttp(err: Error): IO[Response[IO]] = {

    import cats.effect.IO
    import org.http4s.Response
    //def errorToHttp(err: Error): org.http4s.Status = {
    err match {
      case Duplicate(error) => org.http4s.Status.NoContent //(error)
    }
  } */

/* TODO ControlFlowError for control flow solving with errors */
