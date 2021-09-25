package no.perok.toucan.infrastructure.interpreter

import cats.data._
import cats.effect._
import doobie.util.transactor.Transactor
import doobie.implicits._
import no.perok.toucan.domain.algebras.VoteAlgebra
import no.perok.toucan.domain.models._
import no.perok.toucan.infrastructure.repository.{MovieInTroopRepository, VoteRepository}

class VoteInterpreter[F[_]: Sync](xa: Transactor[F]) extends VoteAlgebra[F] {
  def getAllVotesFor(movieInTroop: ID[MovieInTroop]): F[List[Vote]] = ???

  def setVote(
      troop: ID[Troop],
      movie: ID[Movie],
      user: ID[User],
      positive: Boolean
  ): F[Option[ID[Vote]]] = {
    val program = for {
      movieInTroop <- OptionT(MovieInTroopRepository.getMovieInTroopId(troop, movie))
      voteId <- OptionT.liftF(VoteRepository.setVote(movieInTroop, user, positive))
    } yield voteId

    program.value.transact(xa)
  }
}
