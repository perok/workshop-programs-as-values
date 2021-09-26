package no.perok.toucan

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import cats.effect._
import fs2.Stream
import cats._, cats.syntax.all._

@JSImport("Sources/scss/main.scss", JSImport.Default)
@js.native
object assets extends js.Object

@JSImport("uikit", JSImport.Default)
@js.native
@scala.annotation.nowarn
object UIKit extends js.Object {
  def use(in: Any): Unit = js.native
  def notification(in: String): Unit = js.native
}

@js.native
@JSImport("uikit/dist/js/uikit-icons", JSImport.Default)
object UIKitIcons extends js.Object

object ReactApp extends IOApp.Simple {

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def require(): Unit = {
    assets
    UIKit
    UIKitIcons
    ()
  }

  val run: IO[Unit] = {
    require()
    UIKit.use(UIKitIcons)
    UIKit.notification("UIKit loaded")
    IO.unit
  }
}
