package no.perok.toucan.domain.model

final case class MovieInTroop(movie: Movie,
                              watched: Boolean,
                              wantToSee: Option[Boolean],
                              votes: Int)
