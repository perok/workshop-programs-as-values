package no.perok.toucan

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import cats.effect._
import fs2.Stream
import cats._, cats.syntax.all._


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
    IO.pure(ExitCode.Success)
  }
}
