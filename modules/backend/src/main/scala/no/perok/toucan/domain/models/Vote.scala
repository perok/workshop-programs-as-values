package no.perok.toucan.domain.models

final case class Vote(movieInTroopId: ID[MovieInTroop], userId: ID[User], positive: Boolean)
    derives io.circe.Decoder
