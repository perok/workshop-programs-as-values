package no.perok.toucan.domain.model.moviedb

import cats.Show
import io.circe.Decoder

case class TheMovieDbId(id: Int) extends AnyVal // TODO ID[A]

object TheMovieDbId {
  implicit val show: Show[TheMovieDbId] = Show.show(_.id.toString)
// TODO? scala 3 variant import io.circe.generic.extras.semiauto.deriveUnwrappedDecoder
  implicit val decoder: Decoder[TheMovieDbId] = Decoder[Int].map(TheMovieDbId(_))
}
