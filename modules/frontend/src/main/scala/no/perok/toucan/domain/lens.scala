package no.perok.toucan.domain

import no.perok.toucan.domain.model._
import no.perok.toucan.domain.model.moviedb._

object lens {
  import monocle.Lens
  import monocle.macros.GenLens

  val movies: Lens[AppState, Map[TheMovieDbId, MovieDetails]] =
    GenLens[AppState](_.movieDb)

  // movies.asOptional.asFold.composeFold(movies.asFold)

}
