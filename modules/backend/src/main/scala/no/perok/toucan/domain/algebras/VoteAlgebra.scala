package no.perok.toucan.domain.algebras

import no.perok.toucan.domain.models._

trait VoteAlgebra[F[_]]:
  def getAllVotesFor(movieInTroop: ID[MovieInTroop]): F[List[Vote]]
  def setVote(troop: ID[Troop],
              movie: ID[Movie],
              user: ID[User],
              positive: Boolean): F[Option[ID[Vote]]]
