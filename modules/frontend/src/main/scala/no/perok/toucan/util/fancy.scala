// package no.perok.toucan.util

// import cats._
// import cats.data._
// import cats.implicits._
// import cats.effect._
// import japgolly.scalajs.react._
// import japgolly.scalajs.react.vdom._
// import no.perok.toucan.util.stateMachine._

// object fancy {
//   // TODO Kleisli[Eval, Dispatcher[F, Props], ReactPropsProxy[Props]]
//   // eller Reader, men konseptet skal ihvertfall være dette ish
//   // type FancyC[F[_], Props] = State[Dispatcher[F, Props], ReactPropsProxy[Props]]
//   // TODO reader id vs eval?
//   type FancyC[F[_], Props] = Kleisli[Eval, Dispatcher[F, Props], ReactPropsProxy[Props]]
//   // TODO trenger jeg mer enn dette?       Distpacher[F, Props] => ReactPropsProxy[Props]
//   //

//   /*
//   val a: FancyC[IO, Int] = ???
//   val b: FancyC[IO, String] = ???
//   val t: Dispatcher[IO, String] = ???

//   implicit class FancyCOps[F[_], P](
//       private val in: Kleisli[Eval, Dispatcher[F, P], ReactPropsProxy[P]]
//   ) {

//     // Fixes Dispatcher and ReactPropsProxy to correct types for use in for comprehensions
//     // def localMap[PP](f: PP => P): Kleisli[Eval, Dispatcher[F, PP], ReactPropsProxy[PP]] = {
//     //   val lenser: monocle.Lens[PP, P] = monocle.Lens(f)(p => pp => pp)

//     //   // TODO trenger Profunctor?
//     //   in.local[Dispatcher[F, PP]](a => a.zoom(lenser))
//     //     .map(a => a.cmapCtorProps[PP](a => ???): ReactPropsProxy[PP])
//     // }
//     //                            lens?          reactjs props map?
//   }

//   // TODO Dispatcher needs Functor
//   // TODO trait that locks in the props? DSL
//   val z: FancyC[IO, Int] = for {
//     aa <- a
//     // TODO .local utgave som gjør {{bb}} klar for å ta inn props.
//     // altså: Transformasjon på Dispatcher og props inn til VDomNode
//     // .local + .map
//     // må bli extension metode. lens utgave og map utgave
//     // bb <- b.local((_: Dispatcher[IO, Int]) => t)
//     // bb <- b.local[Dispatcher[IO, Int]](_ => t)
//     bbc <- b.localMap[Int](intOut => intOut.toString)
//     // bb <- b.local(a => t)
//     // bb <- b.local(_ => t)

//     // Ny fancy component vil gå inn her... :D
//     //nyFancy <- fancy("nytt nav", ???)

//   } yield pure[Int]("TopApp", props => bb(props))
//    */

//   def apply[F[_], P](name: String, render: (Dispatcher[F, P] => P => VdomNode)): FancyC[F, P] =
//     Kleisli(
//       dispatcher =>
//         Eval.later(
//           ScalaComponent
//             .builder[P](name)
//             .render_P(render(dispatcher))
//             // TODO how to access
//             // .componentDidMount(_ => dispatcher.dispatch(actions.fetchUserData))
//             // .componentDidMount(_ => ???)
//             .build
//         )
//     )

//   // def apply[F[_], P](name: String, render: (Dispatcher[F, P] => P => VdomNode)): FancyC[F, P] =
//   //   State.inspect { dispatcher =>
//   //     ScalaComponent
//   //       .builder[P](name)
//   //       .render_P(render(dispatcher))
//   //       // TODO how to access
//   //       // .componentDidMount(_ => dispatcher.dispatch(actions.fetchUserData))
//   //       // .componentDidMount(_ => ???)
//   //       .build
//   //   }
// }
