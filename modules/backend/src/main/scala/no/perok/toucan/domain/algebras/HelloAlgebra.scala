package no.perok.toucan.domain.algebras

import no.perok.toucan.shared.models.backend.*

trait HelloAlgebra[F[_]]:
  def hello: F[Option[Name]]
