package no.perok.toucan.domain.algebra

import no.perok.toucan.domain.model._
import no.perok.toucan.domain.model.moviedb._

trait AppStateActionsAlgebra[F[_]]:
  def fetchUserData: F[AppState]

  def ensureMovieFetched(id: TheMovieDbId): F[AppState]
  def voteOn(movieInTroopId: MovieInTroopId, vote: Option[Boolean]): F[AppState]
