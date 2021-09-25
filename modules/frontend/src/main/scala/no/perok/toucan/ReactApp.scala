package no.perok.toucan

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import cats.effect._
import fs2.Stream
import slinky.core.facade.ReactElement
// import fs2.concurret._
import cats._, cats.implicits._
// import com.olegpy.shironeko._
// import slinky.core._
// import slinky.web.ReactDOM
import slinky.web.html._
// import no.perok.toucan.view.AppComponent

object CounterActions {
  def increment[F[_]: Monad](implicit S: Store[F]): F[Unit] =
    S.counter.update(_ + 1) >> S.changes.update(_ + 1)

  def decrement[F[_]: Monad](implicit S: Store[F]): F[Unit] =
    S.counter.update(_ - 1) >> S.changes.update(_ + 1)
}

object ReactApp extends IOApp {
  @JSImport("Sources/scss/main.scss", JSImport.Default)
  @js.native
  object assets extends js.Object

  @JSImport("uikit", JSImport.Default)
  @js.native
  object UIKit extends js.Object {
    // @silent
    def use(in: Any): Unit = js.native
    def notification(in: String): Unit = js.native
  }

  @js.native
  @JSImport("uikit/dist/js/uikit-icons", JSImport.Default)
  object UIKitIcons extends js.Object

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def require(): Unit = {
    assets
    UIKit
    UIKitIcons
    ()
  }

  def run(args: List[String]): IO[ExitCode] = {
    require()
    UIKit.use(UIKitIcons)
    UIKit.notification("UIKit loaded")

    object CounterDisplay extends Connector.ContainerNoProps {
      override type State = Int
      override def subscribe[F[_]: Subscribe]: Stream[F, Int] = getAlgebra.counter.discrete
//      subscribe
      override def render[F[_]: Render](state: State): ReactElement = {
        div(
          button(onClick := toCallback { CounterActions.decrement[F] })("-"),
          show"Current value is $state",
          button(onClick := toCallback { getAlgebra.counter.update(_ + 1) })("+")
        )
      }
    }

    Store
      .make[IO]
      .flatMap(
        store =>
          IO.delay {
              // val root = org.scalajs.dom.document.getElementById("container")
              // // val rofl = Connector(store)(new AppComponent[IO](???))
              // val mao = new AppComponent[IO](???)
              // // mao.
              // val rofl = Connector(store)(???)

              // // val rofl: slinky.core.facade.ReactElement = ???
              // ReactDOM.render(rofl, root)
              ()
            }
            .as(ExitCode.Success)
      )

//     // TODO
//     // 1) Do we have token in localstorage? dom.localStorage
//     //   2) if yes
//     //     - Fetch user data from backend
//     //      if nay
//     //     - Api.auth.login()
//     //       - Calls backend with login data
//     //       - Do we have a user?
//     //        TODO
//     // https://github.com/auth0/java-jwt ?
  }
}
