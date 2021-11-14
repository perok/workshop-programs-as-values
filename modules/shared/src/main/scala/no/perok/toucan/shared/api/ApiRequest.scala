package no.perok.toucan.shared.api

import no.perok.toucan.shared.models.backend.Name

object ApiRequest:
  import sttp.tapir._
  import sttp.tapir.json.circe._

  val hello: PublicEndpoint[Unit, Nothing, Name, Any] =
    infallibleEndpoint.get
      .in("hello")
      .out(jsonBody[Name])
      .summary("Say hello to my little endpoint")
