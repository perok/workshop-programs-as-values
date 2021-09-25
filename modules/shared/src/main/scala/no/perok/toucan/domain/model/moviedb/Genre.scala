package no.perok.toucan.domain.model.moviedb

import io.circe.Decoder
import io.circe.derivation.deriveDecoder

case class Genre(name: String)
object Genre {
  implicit val decoder: Decoder[Genre] = deriveDecoder
}
