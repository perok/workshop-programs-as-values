package no.perok.toucan.infrastructure.endpoint

import cats.syntax.flatMap._
import cats.effect.Sync
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import io.circe.syntax.EncoderOps
import no.perok.toucan.domain.algebras.MovieAlgebra

class MovieEndpoint[F[_]: Sync](movieRepository: MovieAlgebra[F]) extends Http4sDsl[F] {
  def service: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "movie" / MovieIdVar(id) =>
        movieRepository.getMovie(id).flatMap {
          case Some(a) => Ok(a.asJson)
          case None => NoContent()
        }
    }
}
