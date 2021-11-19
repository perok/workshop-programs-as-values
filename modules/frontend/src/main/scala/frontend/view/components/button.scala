package frontend.view.components

/** Note: In my experience, optics are absolutely instrumental to writing a scalajs-react UI app
  * where components are nearly all stateless and actually modular and reusable. Have the components
  * ask for as little as possible, use StateSnapshots instead of actual React state and use optics
  * to glue all the layers together. On very large codebases especially, this approach scales very,
  * very well.
  */

import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.all.*

// val Button: ScalaFnComponent.Component[String, CtorType.PropsAndChildren] =
val Button =
  ScalaFnComponent.withChildren[(String, Seq[TagMod])] { case ((lol, tags), children) =>
    div(className := "bg-gray-300 hover:bg-gray-400 text-gray-800 font-bold py-2 px-4 rounded-l",
        lol,
        children
    )
  }
