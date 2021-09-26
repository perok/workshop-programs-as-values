package no.perok.toucan.infrastructure.interpreter

import cats.syntax.all._
import cats.effect._
import doobie.implicits._
import doobie.util.transactor.Transactor
import no.perok.toucan.domain.algebras.MovieAlgebra
import no.perok.toucan.domain.models._
import no.perok.toucan.infrastructure.repository.MovieRepository
import org.http4s.implicits._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.{Query}

class MovieInterpreter[F[_]: Concurrent](xa: Transactor[F], client: Client[F])
    extends MovieAlgebra[F] {

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  private val movieDbUri = uri"https://api.themoviedb.org/3"
  private val query = Query.fromPairs(("api_key", "TODO"))

  @SuppressWarnings(Array("org.wartremover.warts.Null", "org.wartremover.warts.AsInstanceOf"))
  def searchMovie(name: String): F[List[Movie]] = {
    // TODO ScalaCache first
    val uri = (movieDbUri / s"search/movie")
      .copy(
        query = query :+
          (("query", name.some)) :+
          (("include_adult", "true".some)) :+
          (("language", "en".some))
      )

    client
      .expect(uri)(jsonOf[F, String])
      .map { a =>
        pprint.pprintln(a)

        null.asInstanceOf[List[Movie]]
      }
  }

  def getMovie(id: ID[Movie]): F[Option[Movie]] =
    MovieRepository.getMovie(id).transact(xa)

  def insertMovie(newMovie: Movie): F[Either[String, Movie]] =
    MovieRepository.insertMovie(newMovie).transact(xa)
}
