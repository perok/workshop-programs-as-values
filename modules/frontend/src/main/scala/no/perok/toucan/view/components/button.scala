package no.perok.toucan.view.components

import cats.syntax.all._, cats.effect._, cats.effect.syntax.all._
import japgolly.scalajs.react._ // automatically includes ReactCats._
import japgolly.scalajs.react.callback._
import japgolly.scalajs.react.callback.CallbackCatsEffect._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.ReactMonocle._

/** Note: In my experience, optics are absolutely instrumental to writing a scalajs-react UI app
  * where components are nearly all stateless and actually modular and reusable. Have the components
  * ask for as little as possible, use StateSnapshots instead of actual React state and use optics
  * to glue all the layers together. On very large codebases especially, this approach scales very,
  * very well.
  */
class button {
  val test2: AsyncCallback[Unit] = ioToAsyncCallback(IO.unit)

  def onButtonPressed: Callback =
    test2.toCallback
  // Callback.alert("The button was pressed!")

  val lol = <.button(^.onClick --> onButtonPressed, "Press me!")

}
