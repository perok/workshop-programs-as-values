package no.perok.toucan.infrastructure.endpoint

import cats.effect._
import cats.syntax.all._
import io.circe.syntax.EncoderOps
import no.perok.toucan.domain.TroopProgram
import no.perok.toucan.domain.algebras.{TroopAlgebra, VoteAlgebra}
import no.perok.toucan.domain.models._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

//import org.postgresql.util.PSQLException

class TroopEndpoint[F[_]: Concurrent](
    handler: TroopProgram[F],
    troopAlgebra: TroopAlgebra[F],
    voteAlgebra: VoteAlgebra[F]
) extends Http4sDsl[F]:
  // TODO // From unknown program error to a response
  private val errorHandler: PartialFunction[Throwable, F[Response[F]]] =
    //case lol @ PSQLException => Conflict(s"EEEK $lol")
    case lol @ _ => InternalServerError(s"Something bad happened: ${lol.toString()}")

  def endpoints: AuthedRoutes[WithId[User], F] = AuthedRoutes.of[WithId[User], F] {
    case GET -> Root / "troop" as user =>
      troopAlgebra
        .listTroopsFor(user.id)
        .map(a => Response[F]().withEntity(a.asJson))

    case GET -> Root / "troop" / TroopIdVar(id) as _ =>
      // TODO get all troop data? users, movies
      // TODO use user id to ensure person has access
      for {
        troop <- troopAlgebra.getTroop(id)
        result = troop.fold(Response[F](Status.NoContent))(a => Response[F]().withEntity(a.asJson))
      } yield result

    // TODO Add movie (TheMovieDB.Id) to Troop.
    case PUT -> Root / "troop" / TroopIdVar(_) / "movie" as _ =>
      Ok()

    // TODO Add vote on movie in troop from user
    case PUT -> Root / "troop" / TroopIdVar(troop) / "movie" / MovieIdVar(movie) / "vote" / BoolVar(
          vote
        ) as user =>
      voteAlgebra
        .setVote(troop, movie, user.id, vote)
        .map(_.fold(Response[F](Status.NoContent))(a => Response[F]().withEntity(a.asJson)))
    //handler.addVoteToMovie()

    // TODO delete movie from Troop
    case DELETE -> Root / "troop" / TroopIdVar(_) / "movie" / MovieIdVar(_) as _ =>
      Ok() //

    case req @ PUT -> Root / "troop" / TroopIdVar(id) as user =>
      req.req.decodeJson[TroopForm].flatMap { form =>
        handler
          .updateTroop(form, id, user.id)
          // .flatMap(_.fold(a => BadRequest(a.asJson), a => Ok(a.asJson)))
          .map {
            case Right(a) =>
              Response[F]().withEntity(a.asJson)
            case Left(a) =>
              Response[F](Status.BadRequest).withEntity(a.asJson)
          }
      }

    // Create new troop
    case req @ POST -> Root / "troop" as user =>
      req.req.decodeJson[TroopForm].flatMap { form =>
        handler
          .addTroop(form, user.id)
          .flatMap(_.fold(a => BadRequest(a.asJson), a => Ok(a.asJson)))
          .handleErrorWith(errorHandler)
      }
  }
