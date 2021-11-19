package no.perok.toucan.domain

import cats.*
import cats.syntax.all.*
import no.perok.toucan.domain.algebras.*
import no.perok.toucan.shared.models.backend.*

class HelloProgram[F[_]: Monad](helloAlgebra: HelloAlgebra[F]):
  def getHello: F[Name] =
    helloAlgebra.hello.map(_.getOrElse(Name("Ola Nordmann")))
