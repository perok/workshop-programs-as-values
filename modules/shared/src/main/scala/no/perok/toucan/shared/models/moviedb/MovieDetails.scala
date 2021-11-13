package no.perok.toucan.shared.models.moviedb

import io.circe.Decoder

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
) derives Decoder

case class Videos(results: List[Video]) derives Decoder

case class Video(id: String, name: String, site: String, `type`: String, key: String)
    derives Decoder
