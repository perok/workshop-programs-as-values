package no.perok.toucan.domain.model.moviedb

import io.circe.Decoder
import io.circe.derivation.deriveDecoder

case class MovieSearch(id: TheMovieDbId,
                       title: String,
                       overview: String,
                       release_date: String,
                       backdrop_path: Option[String])
object MovieSearch {
  implicit val decoder: Decoder[MovieSearch] = deriveDecoder
}
