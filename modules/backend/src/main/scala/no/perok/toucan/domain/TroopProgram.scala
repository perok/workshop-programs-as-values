package no.perok.toucan.domain

import cats.data.EitherT
import cats.syntax.either._
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.effect._
import no.perok.toucan.domain.models.ExpectedError
import no.perok.toucan.domain.algebras._
import no.perok.toucan.domain.models._

class TroopProgram[F[_]: Sync](troopAlgebra: TroopAlgebra[F], voteAlgebra: VoteAlgebra[F]) {
  //  TODO def setMovieAsWatched(movieInTroop: ID[MovieInTroop]): PResponse[F, Unit]

  def addTroop(newTroop: TroopForm, user: ID[User]): F[ExpectedError Either Troop] =
    (for {
      troop <- EitherT(troopAlgebra.insertTroop(newTroop))
      _ <- EitherT(troopAlgebra.addUserToTroop(troop.id, user, isAdmin = true))
    } yield troop.model).value

  def updateTroop(
      updateTroop: TroopForm,
      troopId: ID[Troop],
      user: ID[User]
  ): F[ExpectedError Either Troop] = {
    /* TODO Step 1: Check for permissions */
    println(user)

    /* TODO Step 2: perform update */
    troopAlgebra.updateTroop(troopId, updateTroop).map(_.asRight)
  }

  def addMovieToTroop(user: ID[User], troopId: ID[Troop], movieId: ID[Movie]): F[ID[MovieInTroop]] =
    for {
      // 1. Have we previously stored this movie?
      // 2. -> Add to movie to troop
      movieInTroop <- troopAlgebra.insertMovie(troopId, movieId)
      // 3. Positive vote for user who added movie
      _ <- voteAlgebra.setVote(troopId, movieId, user, positive = true)
    } yield movieInTroop
}
