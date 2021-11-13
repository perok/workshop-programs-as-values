package no.perok.toucan.shared.models.moviedb

import io.circe.Decoder

case class Genre(name: String) derives Decoder
