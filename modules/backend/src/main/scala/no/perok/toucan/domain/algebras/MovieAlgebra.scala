package no.perok.toucan.domain.algebras

import no.perok.toucan.domain.models._

/* Faktisk film data */
trait MovieAlgebra[F[_]] {
  def searchMovie(name: String): F[List[Movie]]
  def getMovie(id: ID[Movie]): F[Option[Movie]]
  def insertMovie(newMovie: Movie): F[Either[String, Movie]]
}
