// package no.perok.toucan.util

// import cats._
// import cats.data._
// import cats.implicits._
// import cats.effect._
// import japgolly.scalajs.react._
// import japgolly.scalajs.react.vdom._
// import no.perok.toucan.util.stateMachine._

// object experiment {
//   case class Store[F[_]](data: SignallingRef[F, Boolean])

//   object MyActions {
//     def doTheThing[F[_] : BackendAlgebra](implicit S: Store): F[Unit]
//       S.data.modify(_ => F.delay("DERP") >> BackendAlgebra[F].doThang)

//   }

//   case class MyView[F: BackendAlgebra : Store]() extends TopComponent[F]().WithProps[Int] {
//     // Will start to listen for changes in Store
//     // Must be a IO[ReactPropsProxy[Int]]
//     // Will add implicit myStore: Ref[F, Store]

//     val refSubSet: SignallingRef[F, SomeStoreSubSet] = fromStoreConvert // some fund

//     override type State = Boolean

//     // will do descrite on Store[](a:SignallingRef[A])(a => A)
//     //                                                              the signal | fetch subset of data
//     override def subscribe[F[_]]: F[Boolean] = subsriber[Store](_.data)(identity)

//     override def render(store: Boolean, props: Int) = {
//       div("abnc", LesserView(refSubSet)("HELLO WORlD!") onClick := doF { MyActions.doTheThing })
//       // TODO     | Maa fortssatt recreate LesserView hver gang..
//       // Det er dette fancy sin Kleisli Eval løser
//       // eller nei?

//     }
//   }

//   // Must be a ReactPropsProxy[String]
//   // Epects some SignallingRef that implements SomeStoreSubSet
//   case class LesserView[F: BackendAlgebra]()
//     extends  SubComponent[F, SomeStoreSubSet].WithProps[String] {
//     override def render(store: SomeStoreSubSet, props: String) = {}
//   }

// }
