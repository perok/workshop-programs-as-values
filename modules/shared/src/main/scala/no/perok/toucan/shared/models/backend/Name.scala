package no.perok.toucan.shared.models.backend

import io.circe.Codec
import sttp.tapir.Schema

case class Name(name: String) derives Codec.AsObject, Schema
