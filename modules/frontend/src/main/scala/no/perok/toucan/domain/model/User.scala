package no.perok.toucan.domain.model

final case class User(name: String, troops: List[Troop], defaultTroop: Int)
