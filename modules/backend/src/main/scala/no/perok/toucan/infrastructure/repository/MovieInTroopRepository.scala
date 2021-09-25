package no.perok.toucan.infrastructure.repository

import doobie._
import doobie.implicits._
import no.perok.toucan.domain.models._

object MovieInTroopRepository {
  def insertMovie(troopId: ID[Troop], movieId: ID[Movie]): ConnectionIO[ID[MovieInTroop]] = {
    Statements
      .insertMovie(troopId, movieId)
      .withUniqueGeneratedKeys[ID[MovieInTroop]]("id")
//      .attemptSomeSqlState {
//        case sqlstate.class23.UNIQUE_VIOLATION =>
//          PError.Duplicate(s"Movie $movieId already in $troopId")
//      }
  }

  def getMovieInTroopId(troop: ID[Troop],
                        movie: ID[Movie]): ConnectionIO[Option[ID[MovieInTroop]]] =
    ???

  object Statements {
    def insertMovie(troopId: ID[Troop], movieId: ID[Movie]): Update0 =
      sql"""
           |INSERT INTO
           |  troop_has_movie (troop_id, movie_id)
           |VALUES
           |  ($troopId, $movieId)
         """.stripMargin.update
  }
}
