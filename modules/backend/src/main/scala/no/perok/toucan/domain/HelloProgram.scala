package no.perok.toucan.domain

import cats._
import cats.syntax.all._
import no.perok.toucan.domain.algebras._
import no.perok.toucan.shared.models.backend.*

class HelloProgram[F[_]: Monad](helloAlgebra: HelloAlgebra[F]):

  def getHello: F[Name] =
    helloAlgebra.hello.map(_.getOrElse(Name("Ola Nordmann")))
