package no.perok.toucan.domain.algebras

import no.perok.toucan.domain.models._

trait UserAlgebra[F[_]] {
  def getUserHashByUsername(username: String): F[Option[String]]
  def getUserByUsername(username: String): F[Option[WithId[User]]]
  def getUserById(id: ID[User]): F[Option[User]]
  def addUser(newUser: NewUserForm): F[Either[String, Int]]
}
