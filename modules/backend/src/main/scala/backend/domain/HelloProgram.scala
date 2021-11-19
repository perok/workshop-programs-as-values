package backend.domain

import cats.*
import cats.syntax.all.*
import backend.domain.algebras.*
import shared.models.backend.Name

class HelloProgram[F[_]: Monad](helloAlgebra: HelloAlgebra[F]):
  def getHello: F[Name] =
    helloAlgebra.hello.map(_.getOrElse(Name("Ola Nordmann")))
