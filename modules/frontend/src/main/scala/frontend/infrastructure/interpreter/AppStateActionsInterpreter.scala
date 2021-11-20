package frontend.infrastructure.interpreter

import cats.*
import cats.syntax.all.*

import frontend.domain.algebra.AppStateActionsAlgebra
import frontend.domain.model.*
import frontend.infrastructure.*
import shared.api.*

class AppStateActionsInterpreter[F[_]: Monad](requests: Requests[F])
    extends AppStateActionsAlgebra[F] {

  def fetchUserData: F[AppState] =
    requests.performTapirInfallible(ApiRequest.hello)(()).map { result =>
      AppState(result.body.some)
    }

}
