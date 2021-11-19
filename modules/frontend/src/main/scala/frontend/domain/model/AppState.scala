package frontend.domain.model

import shared.models.backend.Name

final case class AppState(helloTo: Option[Name])
