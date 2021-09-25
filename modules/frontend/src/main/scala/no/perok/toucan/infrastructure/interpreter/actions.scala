// package no.perok.toucan.infrastructure.interpreter

// import cats._
// import cats.implicits._
// import cats.effect._
// import monocle.function.all._
// import no.perok.toucan.util.stateMachine._
// import no.perok.toucan.infrastructure.api
// import no.perok.toucan.domain._
// import no.perok.toucan.domain.algebra._
// import no.perok.toucan.domain.model._
// import no.perok.toucan.domain.model.moviedb._

// // TODO move
// // TODO should dispatcher just be moved to use here? THen Action[_, _] is not needed anymore
// // Describtions for transitions on state
// class actionsInter[F[_]: Async: Parallel] extends AppStateActionsAlgebra[F] {
//   def fetchUserData: Action[F, AppState] =
//     ref =>
//       api.internal.tryFetchUserData
//         .flatMap(
//           newUser =>
//             ref
//               .update { oldState =>
//                 val newNav =
//                   NavigationState(
//                     oldState.navigationState.currentTroop.getOrElse(newUser.defaultTroop).some
//                   )

//                 oldState.copy(user = newUser.some, navigationState = newNav)
//               }
//               .map(_ => newUser.troops.traverse(_.movies).flatten)
//         )
//         .flatMap(_.parTraverse(m => ensureMovieFetched(m.movie.id)(ref)))
//         .void

//   def ensureMovieFetched(id: TheMovieDbId): Action[F, AppState] =
//     ref =>
//       ref.get
//         .map(!_.movieDb.contains(id))
//         .ifM(
//           api.moviedb
//             .getMovieDetail(id)
//             .map(details => (lens.movies composeLens at(id)).set(details.some))
//             .flatMap(setter => ref.update(setter(_))),
//           Sync[F].unit
//         )

//   def voteOn(movieInTroopId: MovieInTroopId, vote: Option[Boolean]): Action[F, AppState] = ref => {
// //    ref.
//     // Db op
//     // set result
//     ???
//   }
// }
