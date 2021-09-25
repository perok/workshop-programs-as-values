package no.perok.toucan.domain.models

final case class UserInTroop(userId: ID[User], troopId: ID[Troop], isAdmin: Boolean)
