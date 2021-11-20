package backend.infrastructure.interpreter

import cats.*
import cats.effect.*
import cats.syntax.all.*
import skunk.*

import backend.domain.algebras.*
import backend.infrastructure.repository.*
import shared.models.backend.Name

class HelloInterpreter[F[_]: MonadCancelThrow](session: Resource[F, Session[F]])
    extends HelloAlgebra[F]:
  def hello: F[Option[Name]] =
    session.use(_.option(HelloRepository.getHello))
