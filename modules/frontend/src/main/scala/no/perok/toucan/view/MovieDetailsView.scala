// package no.perok.toucan.view

// import cats.implicits._
// import japgolly.scalajs.react._
// import japgolly.scalajs.react.vdom.html_<^._
// import japgolly.scalajs.react.vdom.html_<^.<._
// import japgolly.scalajs.react.vdom.html_<^.^._
// import no.perok.toucan._
// import no.perok.toucan.domain.model._
// import no.perok.toucan.domain.model.moviedb._

// // TODO Props må ta inn vote :: Boolean -> Callback
// case class MovieDetailsView(movie: MovieInTroop,
//                             details: Option[MovieDetails],
//                             vote: CallbackOn[Option[Boolean]]) {
//   @inline def render: VdomElement =
//     MovieDetailsView.component.withKey(movie.movie.id.id.toLong)(this)
// }

// object MovieDetailsView {
//   private def render(props: MovieDetailsView): VdomElement = {
//     val trailers = props.details
//       .map(_.videos.results)
//       .getOrElse(List.empty)
//       .filter(v => v.`type` === "Trailer" && v.site === "YouTube")
//       .map(v =>
//         div(
//           key := v.id,
//           UIKitAttrs.lightBox.init,
//           a(
//             className := "uk-button uk-button-default",
//             href := show"https://www.youtubenocookie.com/watch?v=${v.key}",
//             VdomAttr("data-caption") := s"Trailer YouTube - ${v.name}",
//             span(UIKitAttrs.icon.init("play")),
//             "View trailer"
//           )
//       ))

//     val coverUrl = props.details.map(_.poster_path).map(p => s"https://image.tmdb.org/t/p/w500/$p")

//     val detailsView =
//       div(
//         className := "uk-panel uk-background-muted uk-padding-small uk-grid-collapse",
//         UIKitAttrs.grid.init,
//         div(
//           className := "uk-width-auto",
//           ul(
//             className := "uk-iconnav uk-iconnav-vertical",
//             // TODO circle around selected
//             li(a(href := "#", UIKitAttrs.icon.init("plus-circle"))),
//             li(a(href := "#", UIKitAttrs.icon.init("minus")))
//           )
//         ),
//         div(
//           className := "uk-width-auto",
//           img(
//             className := "uk-margin-small-left",
//             VdomAttr("data-src") := coverUrl.getOrElse(""),
//             VdomAttr("height") := "50",
//             VdomAttr("width") := "50",
//             VdomAttr("uk-img") := ""
//           )
//         ),
//         div(
//           className := "uk-width-expand",
//           div(className := "uk-text-meta uk-margin-small-left",
//               s"${props.details.map(_.overview).getOrElse("<No description available>")}"),
//         ),
//         div(
//           className := "uk-width-1-1 uk-flex uk-flex-center",
//           trailers.toVdomArray
//         )
//       )

//     li(
//       h4(
//         className := "uk-heading-line uk-margin",
//         div(
//           props.movie.wantToSee
//             .map[VdomNode](wts => span(href := "#", UIKitAttrs.icon.vote(wts)))
//             .getOrElse(span(href := "#", UIKitAttrs.icon.question)),
//           " | ",
//           props.movie.movie.title,
//           span(className := "uk-badge", props.movie.votes)
//         )
//       ),
//       detailsView
//     )
//   }

//   private val component =
//     ScalaComponent
//       .builder[MovieDetailsView]("MovieDetailsView")
//       .render_P(render)
//       // .componentDidMount(info => info.props.loadMovieDetails(info.props.movie.movie.id))
//       .build
// }
