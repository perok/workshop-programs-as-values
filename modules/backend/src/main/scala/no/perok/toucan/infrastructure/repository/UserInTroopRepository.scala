package no.perok.toucan.infrastructure.repository

import doobie._
import doobie.implicits._
import doobie.postgres._
import no.perok.toucan.domain.models._

object UserInTroopRepository {
  def addUserToTroop(
      troop: ID[Troop],
      user: ID[User],
      isAdmin: Boolean
  ): ConnectionIO[ExpectedError Either ID[UserInTroop]] =
    Statements
      .addToTroop(troop, user, isAdmin)
      .withUniqueGeneratedKeys[ID[UserInTroop]]("id")
      .attemptSomeSqlState {
        case sqlstate.class23.UNIQUE_VIOLATION =>
          ExpectedError.Duplicate(s"Troop ${user.toRaw.toString} already exists")
      }

  def getAllTroops(user: ID[User]): ConnectionIO[List[UserInTroop]] =
    Statements.getAllTroops(user).to[List]

  object Statements {
    def addToTroop(troop: ID[Troop], user: ID[User], isAdmin: Boolean): Update0 =
      sql"""
           |INSERT INTO user_is_in_troop
           |  (user_id, troop_id, is_admin)
           |VALUES
           |  ($troop, $user, $isAdmin)
         """.stripMargin.update

    def getAllTroops(user: ID[User]): Query0[UserInTroop] =
      sql"""
           |SELECT
           |  user_id, troop_id, is_admin
           |FROM
           |  user_is_in_troop
           |WHERE
           |  user_id = $user
         """.stripMargin.query[UserInTroop]
  }

}
