package no.perok.toucan.domain.models

final case class MovieInTroop(id: ID[MovieInTroop], troopId: ID[Troop], movieId: ID[Movie])
