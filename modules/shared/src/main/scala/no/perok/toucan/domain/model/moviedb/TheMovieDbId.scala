package no.perok.toucan.domain.model.moviedb

import cats.Show
import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveUnwrappedDecoder

case class TheMovieDbId(id: Int) extends AnyVal // TODO ID[A]

object TheMovieDbId {
  implicit val show: Show[TheMovieDbId] = Show.show(_.id.toString)
  implicit val decoder: Decoder[TheMovieDbId] = deriveUnwrappedDecoder
}
