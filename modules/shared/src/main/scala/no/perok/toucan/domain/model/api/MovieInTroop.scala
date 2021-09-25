package no.perok.toucan.domain.model.api

case class MovieInTroop(movie: Movie, watched: Boolean, wantToSee: Option[Boolean], votes: Int)
