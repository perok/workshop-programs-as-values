// package no.perok.toucan.util

// import cats.data._
// import cats.implicits._
// import cats.effect._
// import cats.effect.concurrent._
// import cats.effect.implicits._
// import fs2._
// import fs2.concurrent.SignallingRef
// import monocle._
// import org.scalajs.dom
// import japgolly.scalajs.react._
// import japgolly.scalajs.react.component.Generic.Mounted
// import japgolly.scalajs.react.component.Scala.{Component, MountedImpure, Unmounted}
// import japgolly.scalajs.react.vdom.VdomElement

// object stateMachine {
//   // TODO othersideloading data? Or scala cache (other user profiles information, etc)

//   type Action[F[_], S] = Ref[F, S] => F[Unit]
//   type ActionP[F[_], Props, S] = (Props, Ref[F, S]) => F[Unit]
//   type ReactProxy[S] = Component[Unit, S, _, CtorType.Nullary]
//   type ReactPropsProxy[P] = Component[P, Unit, _, CtorType.Props]

//   // class dispatcher where
//   //   distpatchA[A] :: (Ref[F, A] => F[Unit]) => Callback
//   //   -- Hvordan vite hva Props er? Kan vel kanskje få hjelp fra Scalajs-react her
//   //   -- Aktive props for akkuratt når ble kalt. (state kan forandre seg mellom, så ikke gi den garantien)
//   //   distpatchProps[A, Props] :: ((Props, Ref[F, A]) => F[Unit]) => Callback
//   // finnesJeg :: Ref[F, A] => Stream[F, A]
//   trait Dispatcher[F[_], S] {
//     def dispatch(f: Action[F, S]): Callback

//     /* You must send in your props manually */
//     def dispatchP[P](props: P)(f: ActionP[F, P, S]): Callback

//     def zoom[S2](z: Lens[S, S2]): Dispatcher[F, S2]
//   }

//   class ConcurrentDispatcher[F[_]: ConcurrentEffect, S](stateRef: Ref[F, S])
//       extends Dispatcher[F, S] {

//     def dispatch(f: Action[F, S]): Callback =
//       ioToCallback(f(stateRef))

//     def dispatchP[P](props: P)(f: ActionP[F, P, S]): Callback =
//       ioToCallback(f(props, stateRef))

//     // TODO this can be split in another module without ConcurrentEffect
//     def zoom[S2](z: Lens[S, S2]): Dispatcher[F, S2] = {
//       val zoomedRef = new Ref[F, S2] {
//         def get: F[S2] = stateRef.get.map(z.get)

//         def set(a: S2): F[Unit] =
//           stateRef.update(s => z.set(a)(s))

//         def getAndSet(s2: S2): F[S2] =
//           stateRef.modify(s1 => (z.set(s2)(s1), z.get(s1)))

//         def access: F[(S2, S2 => F[Boolean])] = {
//           stateRef.access.map {
//             case (s1, s1Setter) =>
//               (z.get(s1), s2 => s1Setter(z.set(s2)(s1)))
//           }
//         }

//         def tryUpdate(f: S2 => S2): F[Boolean] =
//           stateRef.tryUpdate(s => z.set(f(z.get(s)))(s))

//         def tryModify[B](f: S2 => (S2, B)): F[Option[B]] =
//           stateRef.tryModify { s =>
//             val fRes = f(z.get(s))

//             (z.set(fRes._1)(s), fRes._2)
//           }

//         def update(f: S2 => S2): F[Unit] =
//           stateRef.update(s => z.set(f(z.get(s)))(s))

//         def modify[B](f: S2 => (S2, B)): F[B] =
//           stateRef.modify { s =>
//             val fRes = f(z.get(s))

//             (z.set(fRes._1)(s), fRes._2)
//           }

//         def tryModifyState[B](state: State[S2, B]): F[Option[B]] = {
//           val f = state.runF.value

//           tryModify(a => f(a).value)
//         }

//         def modifyState[B](state: State[S2, B]): F[B] = {
//           val f = state.runF.value

//           modify(a => f(a).value)
//         }
//       }

//       new ConcurrentDispatcher[F, S2](zoomedRef)
//     }

//     private def ioToCallback(io: F[Unit]): Callback = Callback {
//       ConcurrentEffect[F].toIO(io).unsafeRunAsync {
//         case Right(_) => ()
//         case Left(err) =>
//           err.printStackTrace()
//       }
//     }
//   }

//   // TODO
//   //  - should be able to place on arbitraty locs in app
//   //  - Handle observing of sink here?
//   //  - observeLast? trenger kun nyeste snapshot for hver gang react har tid til aa rendre
//   //    - todo er det ikke noe onRenderReadygreier man kan legge til at man puller state fra? Er vel Pull type class for slikt? Pull og latest. Kun det man trenger :D
//   //    - Pull
//   def connect[F[_]: ConcurrentEffect, S](
//       stateSignal: SignallingRef[F, S],
//       viewF: ReactPropsProxy[S]): F[(Deferred[F, Stream[F, S]], ReactProxy[S])] = {
//     // viewF.>>>(_ => 1)

//     // TODO bug @Suppresswarning mot metals her. Rapporter? scalameta
//     @SuppressWarnings(Array("org.wartremover.warts.Var"))
//     class AppBackend($ : BackendScope[Unit, S]) {
//       // TODO wrap in Ref. Change with F[A] ~ Callback[A]
//       var setter: Option[S => F[Unit]] = None
//       val updateSink: Pipe[F, S, Unit] =
//         _.evalMap(newState => setter.map(f => f(newState)).getOrElse(Sync[F].unit))
//       /*
//         TODO Stream that emits the setter func
//           updateFunc: Option[S => Unit]
//         when Some then run use updateSink
//         else abort "intteruptWhen" changed when changed

//         this design can remove all update details from React setup?
//         - Must be a SignallingRef

//         ~Stream[F, Option[S => Unit]]~
//          Stream[F, Option[S => F[Unit]]] <-- Tving API til å gi fra seg setState wrappet i F
//        */

//       def willMount = Callback {
//         // SignallingRef(None)
//         val lol: Stream[F, Option[S => Unit]] = Stream.emit(None)

//         setter = Some(newState => Sync[F].delay($.setState(newState).runNow()))
//       }

//       def willUnmount = Callback {
//         setter = None
//       }

//       // TODO pass down dispatcher
//       def render(state: S): VdomElement = viewF(state)
//     }

//     for {
//       initialState <- stateSignal.get
//       deferred <- Deferred[F, Stream[F, S]]
//     } yield {
//       val component = ScalaComponent
//         .builder[Unit]("FS2SignalWrapper")
//         .initialState(initialState)
//         .backend { $ : BackendScope[Unit, S] =>
//           val backend = new AppBackend($)
//           val signalObserver = stateSignal.discrete.observe(backend.updateSink)

//           // Hand over the observable after having mounted the component
//           ConcurrentEffect[F].toIO(deferred.complete(signalObserver)).unsafeRunAsync {
//             case Right(_) => println("Backend mounted")
//             case Left(err) => err.printStackTrace()
//           }

//           backend
//         }
//         .renderBackend
//         .componentWillMount(_.backend.willMount)
//         .componentWillUnmount(_.backend.willUnmount)
//         .build

//       (deferred, component)
//     }
//   }

//   // subscribe
//   // TODO ClassOfSomethingToSubscribeOn
//   // -- Zooming State, with zoomed dispatcher
//   // -- Join multiple other of these
//   // -- runIt (Proxy => Unit)
//   def subscribe[F[_]: ConcurrentEffect, S](
//       initialState: S,
//       initialAction: Action[F, S],
//       initialRenderer: Dispatcher[F, S] => ReactPropsProxy[S]): F[ExitCode] =
//     for {
//       stateSignal <- SignallingRef[F, S](initialState)

//       renderer = initialRenderer(new ConcurrentDispatcher[F, S](stateSignal))

//       initialActionFiber <- initialAction(stateSignal).start

//       // TODO dispatcher må deles til alle under dispatcher: Dispatcher[AppState],
//       (deferredStream, component) <- connect[F, S](stateSignal, renderer)
//       _ = { //                  | Mounted?
//         // TODO (VDomElement => Unit) => Deferred[F, Sink[F, S]] ???
//         val zz: Unmounted[Unit, S, _] = component()

//         val z: MountedImpure[Unit, S, _] =
//           component().renderIntoDOM(dom.document.getElementById("container"))

//         val g = z.backend // TODO B i MountedPure|Impure vil gi backend som igjen kan gi sink !. Kan unngå deferred n friends

//         component().renderIntoDOM(dom.document.getElementById("container"))
//       }
//       _ <- initialActionFiber.join
//       stream <- deferredStream.get
//       _ = println("Kicking off signal subscription")
//       exitCode <- stream.compile.drain.as(ExitCode.Success)
//       _ = println(s"Closing app with $exitCode")
//     } yield exitCode
// }
