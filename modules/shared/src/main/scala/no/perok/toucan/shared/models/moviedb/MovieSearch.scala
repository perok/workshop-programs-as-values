package no.perok.toucan.shared.models.moviedb

import io.circe.Decoder

case class MovieSearch(id: TheMovieDbId,
                       title: String,
                       overview: String,
                       release_date: String,
                       backdrop_path: Option[String]
) derives Decoder
