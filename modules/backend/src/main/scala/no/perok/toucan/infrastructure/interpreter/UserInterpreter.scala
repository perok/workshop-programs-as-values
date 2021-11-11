package no.perok.toucan.infrastructure.interpreter

import cats.effect._
import doobie.implicits._
import doobie.util.transactor.Transactor
import no.perok.toucan.domain.algebras.UserAlgebra
import no.perok.toucan.domain.models._
import no.perok.toucan.infrastructure.repository.UserRepository

class UserInterpreter[F[_]: Sync](xa: Transactor[F]) extends UserAlgebra[F]:
  def getUserHashByUsername(username: String): F[Option[String]] =
    UserRepository.getUserHashByUsername(username).transact(xa)

  def getUserByUsername(username: String): F[Option[WithId[User]]] =
    UserRepository.getUserByUsername(username).transact(xa)

  def getUserById(id: ID[User]): F[Option[User]] =
    UserRepository.getUserById(id).transact(xa)

  def addUser(newUser: NewUserForm): F[Either[String, Int]] =
    UserRepository.addUser(newUser).transact(xa)
