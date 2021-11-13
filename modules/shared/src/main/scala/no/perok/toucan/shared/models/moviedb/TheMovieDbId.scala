package no.perok.toucan.shared.models.moviedb

import cats.Show
import io.circe.Decoder

case class TheMovieDbId(id: Int) extends AnyVal // TODO ID[A]

object TheMovieDbId:
  implicit val show: Show[TheMovieDbId] = Show.show(_.id.toString)
  implicit val decoder: Decoder[TheMovieDbId] = Decoder[Int].map(TheMovieDbId(_))
