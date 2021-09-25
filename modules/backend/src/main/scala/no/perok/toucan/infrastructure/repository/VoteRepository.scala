package no.perok.toucan.infrastructure.repository

import doobie._
import doobie.implicits._
import no.perok.toucan.domain.models._

object VoteRepository {
  def getAllVotesFor(movieInTroop: ID[MovieInTroop]): ConnectionIO[List[Vote]] =
    Statements.getAllVotesFor(movieInTroop).to[List]

  def setVote(
      movieInTroop: ID[MovieInTroop],
      user: ID[User],
      positive: Boolean
  ): ConnectionIO[ID[Vote]] = {
    Statements
      .setVote(movieInTroop, user, positive)
      .withUniqueGeneratedKeys[ID[Vote]]("id")
//      .attemptSomeSqlState {
//        case sqlstate.class23.UNIQUE_VIOLATION =>
//          PError.Duplicate(s"User $user has already voted on $movieInTroop")
//      }
  }

  object Statements {
    def getAllVotesFor(movieInTroop: ID[MovieInTroop]): Query0[Vote] =
      sql"""
           |SELECT id, troop_movie_id, user_id, positive
           |FROM movie_in_troop_has_user_vote
           |WHERE
           |  troop_movie_id = $movieInTroop
         """.stripMargin.query

    def setVote(movieInTroop: ID[MovieInTroop], user: ID[User], positive: Boolean): Update0 =
      sql"""
           |INSERT INTO
           | movie_in_troop_has_user_vote (troop_movie_id, user_id, positive)
           |VALUES ($movieInTroop, $user, $positive)
           |ON DUPLICATE KEY
           | update positive=$positive
      """.stripMargin.update
  }
}
