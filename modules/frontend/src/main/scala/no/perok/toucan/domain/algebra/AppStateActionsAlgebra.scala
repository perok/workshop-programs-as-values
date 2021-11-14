package no.perok.toucan.domain.algebra

// TODO frontend namespace
import no.perok.toucan.domain.model.*

trait AppStateActionsAlgebra[F[_]]:
  def fetchUserData: F[AppState]
