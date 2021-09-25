// package no.perok.toucan.view

// import cats.implicits._
// import japgolly.scalajs.react._
// import japgolly.scalajs.react.vdom.html_<^._
// import japgolly.scalajs.react.vdom.html_<^.<._
// import japgolly.scalajs.react.vdom.html_<^.^._
// import no.perok.toucan._
// import no.perok.toucan.domain.model._
// import no.perok.toucan.domain.model.moviedb._

// case class TroopComponent(state: AppState,
//                           troop: Troop,
//                           vote: MovieInTroopId => CallbackOn[Option[Boolean]]) {
//   @inline def render: VdomElement = TroopComponent.component(this)
// }

// object TroopComponent {
//   private def render(props: TroopComponent): VdomElement = {
//     val movieDetailsView: List[MovieDetailsView] =
//       props.troop.movies.sortBy(_.votes).map { movie =>
//         MovieDetailsView(movie,
//                          props.state.movieDb.get(movie.movie.id),
//                          props.vote(MovieInTroopId(2)))
//       }

//     div(
//       h1(className := "uk-heading-primary uk-heading-divider", props.troop.name),
//       ul(UIKitAttrs.accordion.init, movieDetailsView.toVdomArray(a => a.render))
//     )
//   }

//   private val component =
//     ScalaComponent
//       .builder[TroopComponent]("Troop")
//       .render_P(render)
//       .build
// }
