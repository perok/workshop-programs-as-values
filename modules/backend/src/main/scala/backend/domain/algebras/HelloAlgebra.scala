package backend.domain.algebras

import shared.models.backend.Name

trait HelloAlgebra[F[_]]:
  def hello: F[Option[Name]]
