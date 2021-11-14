package no.perok.toucan.infrastructure.interpreter

import cats.*
import cats.syntax.all.*
import no.perok.toucan.domain.algebra.AppStateActionsAlgebra
import no.perok.toucan.domain.model.*
import no.perok.toucan.infrastructure.*
import no.perok.toucan.shared.api.*

class AppStateActionsInterpreter[F[_]: Monad](requests: Requests[F])
    extends AppStateActionsAlgebra[F] {

  def fetchUserData: F[AppState] =
    requests.performTapirInfallible(ApiRequest.hello)(()).map { result =>
      AppState(result.body.some)
    }

}
