package no.perok.toucan

import cats.*
import cats.effect.*
import cats.syntax.all.*
import fs2.Stream

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import no.perok.toucan.infrastructure.interpreter.AppStateActionsInterpreter

@JSImport("Sources/css/style.css", JSImport.Default)
@js.native
object assets extends js.Object

// @JSImport("uikit", JSImport.Default)
// @js.native
// @scala.annotation.nowarn
// object UIKit extends js.Object:
//   def use(in: Any): Unit = js.native
//   def notification(in: String): Unit = js.native

// @js.native
// @JSImport("uikit/dist/js/uikit-icons", JSImport.Default)
// object UIKitIcons extends js.Object

object Main:
  import japgolly.scalajs.react.*
  // import japgolly.scalajs.react.vdom.html_<^.*
  import japgolly.scalajs.react.vdom.all.*
  import japgolly.scalajs.react.ReactMonocle.*
  import japgolly.scalajs.react.extra.StateSnapshot
  import monocle.*

  val NameChanger = ScalaComponent
    .builder[StateSnapshot[String]]("Name changer")
    .render_P { ss =>
      def updateName = (event: ReactEventFromInput) => ss.setState(event.target.value)

      input.text(value := ss.value, onChange ==> updateName)
    }
    .build

  final case class Name(firstName: String, surname: String)

  val Main = ScalaComponent
    .builder[Unit]
    .initialState[Name](Name("John", "Wick"))
    .render { $ =>
      val name = $.state
      val firstNameV = StateSnapshot.zoomL(Focus[Name](_.firstName)).of($)
      val surnameV = StateSnapshot.zoomL(Focus[Name](_.surname)).of($)

      val rofl =
        IO.pure("New name").flatMap(newName => $.modStateAsync(_.copy(firstName = newName)))

      div(
        label("First name:", NameChanger(firstNameV)),
        label("Surname:", NameChanger(surnameV)),
        button(
          onClick --> rofl,
          "Set new name async!"
        ),
        lol2("Test functional component"),
        Component(),
        p(s"My name is ${name.surname}, ${name.firstName} ${name.surname}.")
      )
    }
    .componentDidMount(c => c.modStateAsync(a => a.copy(surname = "Loaded")))
    .build

  lazy val lol2: ScalaFnComponent.Component[String, CtorType.Props] =
    ScalaFnComponent[String](in => div(in))

  lazy val Component: ScalaFnComponent.Component[Unit, CtorType.Nullary] = ScalaFnComponent
    .withHooks[Unit]
    .useState(0)
    .useEffectBy((props, count) =>
      IO.blocking {
        org.scalajs.dom.document.title = s"You clicked ${count.value} times"
      }
    )
    .useState("banana")
    .render((props, count, fruit) =>
      div(
        p(s"You clicked ${count.value} times"),
        view.components.Button("Lol", Seq(onClick --> count.modState(_ + 1)))(),
        button(
          className := "bg-gray-300 hover:bg-gray-400 text-gray-800 font-bold py-2 px-4 rounded-l",
          onClick --> count.modState(_ + 1),
          "Click me"
        ),
        p(s"Your favourite fruit is a ${fruit.value}!")
      )
    )

object ReactApp extends IOApp.Simple:
  def require(): Unit =
    assets

  def run: IO[Unit] =
    require()

    import no.perok.toucan.shared.models.*
    import no.perok.toucan.infrastructure.*

    val requests = Requests[IO]
    val appState = new AppStateActionsInterpreter(requests)

    import japgolly.scalajs.react.vdom.all.*
    import org.scalajs.dom.document

    val Root = div(
      h1("Hello", className := "bg-red-900 text-white"),
      button("Hei", onClick --> appState.fetchUserData.void),
      ol(id := "my-list", lang := "en", margin := 8.px, li("Item 1"), li("Item 2")),
      Main.Main()
    )

    IO.delay(Root.renderIntoDOM(document.getElementById("root"))) >>
      IO.never
