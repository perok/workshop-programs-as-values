package no.perok.toucan.infrastructure.interpreter

import cats.effect._
import doobie.implicits._
import doobie.util.transactor.Transactor
import no.perok.toucan.domain.algebras.TroopAlgebra
import no.perok.toucan.domain.models._
import no.perok.toucan.infrastructure.repository.{MovieInTroopRepository, UserInTroopRepository}

class TroopInterpreter[F[_]: Sync](xa: Transactor[F]) extends TroopAlgebra[F] {
  def listTroops: F[List[Troop]] = ???

  def getTroop(id: ID[Troop]): F[Option[Troop]] = ???

  def updateTroop(id: ID[Troop], newTroop: TroopForm): F[Troop] = ???

  def insertTroop(newTroop: TroopForm): F[Either[ExpectedError, WithId[Troop]]] = ???

  def insertMovie(troopId: ID[Troop], movieId: ID[Movie]): F[ID[MovieInTroop]] = {
    MovieInTroopRepository.insertMovie(troopId, movieId).transact(xa)
  }

  def addUserToTroop(
      troop: ID[Troop],
      user: ID[User],
      isAdmin: Boolean
  ): F[Either[ExpectedError, ID[UserInTroop]]] = {
    UserInTroopRepository.addUserToTroop(troop, user, isAdmin).transact(xa)
  }

  def listTroopsFor(user: ID[User]): F[List[UserInTroop]] =
    UserInTroopRepository.getAllTroops(user).transact(xa)
}
