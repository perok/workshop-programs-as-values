package no.perok.toucan.domain

import cats._
import cats.data.EitherT
import cats.syntax.all._
import no.perok.toucan.domain.algebras._
import no.perok.toucan.domain.models.{ExpectedError, _}

class TroopProgram[F[_]: Monad](troopAlgebra: TroopAlgebra[F], voteAlgebra: VoteAlgebra[F]):
  def addTroop(newTroop: TroopForm, user: ID[User]): F[Either[ExpectedError, Troop]] =
    val program = for {
      troop <- EitherT(troopAlgebra.insertTroop(newTroop))
      _ <- EitherT(troopAlgebra.addUserToTroop(troop.id, user, isAdmin = true))
    } yield troop.model

    program.value

  def updateTroop(
      updateTroop: TroopForm,
      troopId: ID[Troop],
      user: ID[User]
  ): F[ExpectedError Either Troop] =
    /* TODO Step 1: Check for permissions */
    println(user)

    /* TODO Step 2: perform update */
    troopAlgebra.updateTroop(troopId, updateTroop).map(_.asRight)

  def addMovieToTroop(user: ID[User], troopId: ID[Troop], movieId: ID[Movie]): F[ID[MovieInTroop]] =
    for {
      // 1. Have we previously stored this movie?
      // 2. -> Add to movie to troop
      movieInTroop <- troopAlgebra.insertMovie(troopId, movieId)
      // 3. Positive vote for user who added movie
      _ <- voteAlgebra.setVote(troopId, movieId, user, positive = true)
    } yield movieInTroop
