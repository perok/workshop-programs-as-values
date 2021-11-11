package no.perok.toucan.domain.model.moviedb

import io.circe.Decoder

case class Genre(name: String) derives Decoder
