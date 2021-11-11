package no.perok.toucan

import cats._
import cats.effect._
import cats.syntax.all._
import fs2.Stream

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("Sources/scss/main.scss", JSImport.Default)
@js.native
object assets extends js.Object

@JSImport("uikit", JSImport.Default)
@js.native
@scala.annotation.nowarn
object UIKit extends js.Object:
  def use(in: Any): Unit = js.native
  def notification(in: String): Unit = js.native

@js.native
@JSImport("uikit/dist/js/uikit-icons", JSImport.Default)
object UIKitIcons extends js.Object

object Main:
  import japgolly.scalajs.react._
  // import japgolly.scalajs.react.vdom.html_<^._
  import japgolly.scalajs.react.vdom.all._
  import japgolly.scalajs.react.ReactMonocle._
  import japgolly.scalajs.react.extra.StateSnapshot
  import monocle._

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
        button(
          onClick --> count.modState(_ + 1),
          "Click me"
        ),
        p(s"Your favourite fruit is a ${fruit.value}!")
      )
    )

object utils:
  import sttp.tapir.client.sttp.SttpClientInterpreter
  import sttp.client3.impl.cats.FetchCatsBackend

  val tapirInterpreter = SttpClientInterpreter()
  val backend = FetchCatsBackend[IO]()

  def performTapirE[I, E, O, R](
      e: sttp.tapir.PublicEndpoint[I, E, O, Any]
  ): I => IO[sttp.client3.Response[Either[E, O]]] = { input =>
    val req = tapirInterpreter
      .toRequestThrowDecodeFailures(e, None)
      .apply(input)

    backend.send(req)
  }

  def performTapir[I, O, R](
      e: sttp.tapir.PublicEndpoint[I, Nothing, O, Any]
  ): I => IO[sttp.client3.Response[O]] = { input =>
    performTapirE[I, Nothing, O, R](e)(input).map { result =>
      val newBody = result.body match
        case Right(res) => res
        case Left(_) => throw new Exception("Impossible")

      result.copy(body = newBody)
    }
  }

object ReactApp extends IOApp.Simple:
  def require(): Unit =
    assets
    UIKit
    UIKitIcons
    ()

  val run: IO[Unit] =
    require()
    UIKit.use(UIKitIcons)
    UIKit.notification("UIKit loaded")

    import no.perok.toucan.domain.model.ApiRequest
    import no.perok.toucan.domain.model.ApiRequest._

    val result =
      utils.performTapirE(ApiRequest.booksListing)((BooksFromYear("", 1), 10, ""))

    import japgolly.scalajs.react.vdom.all._
    import org.scalajs.dom.document

    val Root = div(
      button("Hei", onClick --> result.void),
      ol(id := "my-list", lang := "en", margin := 8.px, li("Item 1"), li("Item 2")),
      Main.Main()
    )

    IO.delay(Root.renderIntoDOM(document.getElementById("container"))) >>
      IO.never
