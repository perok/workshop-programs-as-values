package no.perok.toucan.domain.models

final case class Troop(name: String) //, registeredAt: Instant)
    derives io.circe.Decoder,
      io.circe.Encoder.AsObject
