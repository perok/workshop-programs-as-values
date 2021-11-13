package no.perok.toucan.infrastructure

import io.circe._
import no.perok.toucan.domain.models._
import org.http4s.BasicCredentials

package object endpoint:

  object TroopIdVar:
    import org.http4s.dsl.impl.IntVar
    def unapply(str: String): Option[ID[Troop]] =
      IntVar.unapply(str).map(ID[Troop])

  object BoolVar:
    import scala.util.Try
    def unapply(str: String): Option[Boolean] =
      Try(str.toBoolean).toOption

  object MovieIdVar:
    import org.http4s.dsl.impl.IntVar
    def unapply(str: String): Option[ID[Movie]] =
      IntVar.unapply(str).map(ID[Movie])

  implicit val errorEncoder: Encoder[ExpectedError] = Encoder.instance {
    // TODO PERROR => PHTTPERR // Alle programfeil blir representert som det
    case unknown @ ExpectedError.Unknown => io.circe.Json.fromString(unknown.error)
    case ExpectedError.Duplicate(error) => io.circe.Json.fromString(error)
  }
