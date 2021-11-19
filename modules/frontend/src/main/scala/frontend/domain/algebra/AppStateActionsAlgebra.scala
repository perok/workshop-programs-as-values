package frontend.domain.algebra

// TODO frontend namespace
import frontend.domain.model.*

trait AppStateActionsAlgebra[F[_]]:
  def fetchUserData: F[AppState]
