package no.perok.toucan.domain.model.moviedb

import io.circe.Decoder
import io.circe.derivation.deriveDecoder

case class MovieDetails(
    id: TheMovieDbId,
    imdb_id: String,
    backdrop_path: Option[String],
    budget: Option[Int],
    genres: List[Genre],
    overview: String,
    tagline: String,
    title: String,
    video: Boolean,
    original_language: String,
    original_title: String,
    poster_path: String,
    release_date: String,
    runtime: Int,
    status: String, // ADT Allowed Values: Rumored, Planned, In Production, Post Production, Released, Canceled
    vote_average: Double,
    vote_count: Int,
    videos: Videos
)

object MovieDetails {
  implicit val decoder: Decoder[MovieDetails] = deriveDecoder
}

case class Videos(results: List[Video])
object Videos {
  implicit val decoder: Decoder[Videos] = deriveDecoder
}

case class Video(id: String, name: String, site: String, `type`: String, key: String)
object Video {
  implicit val decoder: Decoder[Video] = deriveDecoder
}
