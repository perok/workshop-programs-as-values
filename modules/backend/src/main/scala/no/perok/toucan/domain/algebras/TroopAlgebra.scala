package no.perok.toucan.domain.algebras

import no.perok.toucan.domain.models.*

trait TroopAlgebra[F[_]]:
  def listTroops: F[List[Troop]]
  def listTroopsFor(user: ID[User]): F[List[UserInTroop]]
  def getTroop(id: ID[Troop]): F[Option[Troop]]
  def updateTroop(id: ID[Troop], newTroop: TroopForm): F[Troop]
  def insertTroop(newTroop: TroopForm): F[Either[ExpectedError, WithId[Troop]]]
  def insertMovie(troopId: ID[Troop], movieId: ID[Movie]): F[ID[MovieInTroop]]
  def addUserToTroop(troop: ID[Troop],
                     user: ID[User],
                     isAdmin: Boolean
  ): F[ExpectedError Either ID[UserInTroop]]
