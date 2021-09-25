// package no.perok.toucan.util

// import japgolly.scalajs.react._
// import japgolly.scalajs.react.vdom._

// import no.perok.toucan.ComponentDefinition
// import no.perok.toucan.util.stateMachine._

// object pure {
//   def apply[P](name: String, render: P => VdomNode): ReactPropsProxy[P] =
//     ScalaComponent
//       .builder[P](name)
//       .render_P(render)
//       // TODO how to access
//       // .componentDidMount(_ => dispatcher.dispatch(actions.fetchUserData))
//       // .componentDidMount(_ => ???)
//       .build
// }
