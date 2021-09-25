package no.perok.toucan.infrastructure

import io.circe._
import io.circe.derivation._
import no.perok.toucan.domain.models._
import org.http4s.BasicCredentials

package object endpoint {

  object TroopIdVar {
    import org.http4s.dsl.impl.IntVar
    def unapply(str: String): Option[ID[Troop]] =
      IntVar.unapply(str).map(ID[Troop])
  }

  object BoolVar {
    import scala.util.Try
    def unapply(str: String): Option[Boolean] =
      Try(str.toBoolean).toOption
  }

  object MovieIdVar {
    import org.http4s.dsl.impl.IntVar
    def unapply(str: String): Option[ID[Movie]] =
      IntVar.unapply(str).map(ID[Movie])
  }

  // TODO move to classes
  implicit val userEncoder: Encoder[User] = deriveEncoder
  implicit val userDecoder: Decoder[User] = deriveDecoder

  implicit val basicCredentialsDecoder: Decoder[BasicCredentials] = deriveDecoder

  implicit val newUserFormDecoder: Decoder[NewUserForm] = deriveDecoder

  implicit val troopFormDecoder: Decoder[TroopForm] = deriveDecoder

  implicit val errorEncoder: Encoder[ExpectedError] = Encoder.instance {
    // TODO PERROR => PHTTPERR // Alle programfeil blir representert som det
    case unknown @ ExpectedError.Unknown => io.circe.Json.fromString(unknown.error)
    case ExpectedError.Duplicate(error) => io.circe.Json.fromString(error)
  }

  implicit val troopEncoder: Encoder[Troop] = deriveEncoder
  implicit val troopDecoder: Decoder[Troop] = deriveDecoder

  implicit val userInTroopEncoder: Encoder[UserInTroop] = deriveEncoder
  implicit val userInTroopDecoder: Decoder[UserInTroop] = deriveDecoder

  implicit val voteEncoder: Encoder[Vote] = deriveEncoder
  implicit val voteDecoder: Decoder[Vote] = deriveDecoder

  implicit val movieEncoder: Encoder[Movie] = deriveEncoder
  implicit val movieDecoder: Decoder[Movie] = deriveDecoder
}
