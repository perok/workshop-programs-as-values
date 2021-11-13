package no.perok.toucan.shared.models

case class User(name: String, troops: List[Troop], defaultTroop: Int)
