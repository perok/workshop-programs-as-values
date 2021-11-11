package no.perok.toucan.infrastructure.endpoint

import cats.effect.Sync
import cats.syntax.all._
import io.circe.syntax.EncoderOps
import no.perok.toucan.domain.algebras.MovieAlgebra
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class MovieEndpoint[F[_]: Sync](movieRepository: MovieAlgebra[F]) extends Http4sDsl[F] {
  def service: HttpRoutes[F] =
    HttpRoutes.of[F] { case GET -> Root / "movie" / MovieIdVar(id) =>
      movieRepository.getMovie(id).map {
        case Some(a) => Response[F]().withEntity(a.asJson)
        case None => Response(Status.NoContent)
      }
    }
}
