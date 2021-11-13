package no.perok.toucan.shared.models

case class MovieInTroop(movie: Movie, watched: Boolean, wantToSee: Option[Boolean], votes: Int)
