package no.perok.toucan.infrastructure.repository

import doobie._
import doobie.implicits._
import doobie.postgres._
import no.perok.toucan.domain.models._

/*object tesst {
    import cats._
    import cats.data._

    import no.perok.toucan.infrastructure.repository.dao._

    // TODO hvordan kombinere flere
    def recosForUser[F[_]](userId: ID[Movie])
                          (implicit F: Monad[F]): Kleisli[F, MovieRepository[F], Seq[Movie]] =
      for {
        user  <- Kleisli[F, MovieRepository[F], Option[Movie]](_.getMovie(userId))
        _ <- Kleisli[F, MovieRepository[F], Either[String, Movie]](_.insertMovie(user.get))
      } yield user.toSeq
  }*/

object MovieRepository:
  def getMovie(id: ID[Movie]): ConnectionIO[Option[Movie]] =
    Statements.getMovie(id).option

  /*
private val errorHandler: PartialFunction[SqlState, String] = {
  //case UserNotFoundException(id)              => BadRequest(s"User with id: $id not found!")
  //case DuplicatedUsernameException(username)  => Conflict(s"Username $username already in use!")
  case sqlstate.class23.UNIQUE_VIOLATION => "egg"
}*/

  def insertMovie(newMovie: Movie): ConnectionIO[String Either Movie] =
    Statements
      .insertMovie(newMovie)
      .withUniqueGeneratedKeys[Movie]("id", "title", "data")
      .attemptSomeSqlState {
        case sqlstate.class23.UNIQUE_VIOLATION =>
          s"Movie ${newMovie.title} is already stored"
      }

  object Statements:
    def getMovie(id: ID[Movie]): Query0[Movie] =
      sql"select id, title, data from movie where id = $id"
        .query[Movie]

    def insertMovie(newMovie: Movie): Update0 =
      sql"""insert into
           movie (id, title)
          values
            (${newMovie.id}, ${newMovie.title})
      """.update
