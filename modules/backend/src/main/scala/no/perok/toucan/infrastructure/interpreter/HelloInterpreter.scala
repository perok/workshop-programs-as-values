package no.perok.toucan.infrastructure.interpreter

import cats.*
import cats.effect._
import cats.syntax.all._
import no.perok.toucan.domain.algebras.*
import no.perok.toucan.shared.models.backend.*
import no.perok.toucan.infrastructure.repository.*
import skunk.*

class HelloInterpreter[F[_]: MonadCancelThrow](session: Resource[F, Session[F]])
    extends HelloAlgebra[F]:
  def hello: F[Option[Name]] =
    session.use(_.option(HelloRepository.getHello))
