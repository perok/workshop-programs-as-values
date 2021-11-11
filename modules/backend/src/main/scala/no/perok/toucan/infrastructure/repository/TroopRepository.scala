package no.perok.toucan.infrastructure.repository

import doobie._
import doobie.implicits._
import doobie.postgres._
import no.perok.toucan.domain.models._

object TroopRepository:
  val listTroops: ConnectionIO[List[Troop]] =
    Statements.listTroops.to[List]

  def getTroop(id: ID[Troop]): ConnectionIO[Option[Troop]] =
    Statements.getTroop(id).option

  def updateTroop(id: ID[Troop], newTroop: TroopForm): ConnectionIO[Troop] =
    Statements
      .updateTroop(id, newTroop)
      .withUniqueGeneratedKeys[Troop]("id", "name")

  /* TODO til min error klasse
private val errorHandler: PartialFunction[SqlState, String] = {
  //case UserNotFoundException(id)              => BadRequest(s"User with id: $id not found!")
  //case DuplicatedUsernameException(username)  => Conflict(s"Username $username already in use!")
  case sqlstate.class23.UNIQUE_VIOLATION => "egg"
}*/

  def insertTroop(newTroop: TroopForm): ConnectionIO[ExpectedError Either Troop] =
    Statements
      .insertTroop(newTroop)
      .withUniqueGeneratedKeys[Troop]("id", "name")
      .attemptSomeSqlState {
        case sqlstate.class23.UNIQUE_VIOLATION =>
          ExpectedError.Duplicate(s"Troop ${newTroop.name} already exists")
      }

  object Statements:
    val listTroops: Query0[Troop] =
      sql"select name from troop"
        .query[Troop]

    def getTroop(id: ID[Troop]): Query0[Troop] =
      sql"select name from troop where id = $id"
        .query[Troop]

    def updateTroop(id: ID[Troop], newTroop: TroopForm): Update0 =
      sql"update troop set name=${newTroop.name} where id=$id".update

    def insertTroop(newTroop: TroopForm): Update0 =
      sql"insert into troop (name) values (${newTroop.name})".update
