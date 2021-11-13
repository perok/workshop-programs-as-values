package no.perok.toucan.domain.model

import no.perok.toucan.shared.models.moviedb._

final case class AppState(user: Option[User],
                          navigationState: NavigationState,
                          movieDb: Map[TheMovieDbId, MovieDetails]
)
