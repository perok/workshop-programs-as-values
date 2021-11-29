package frontend

import cats.*
import cats.effect.*
import cats.effect.std.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import fs2.Stream
import weaver.*

import scala.concurrent.duration.*
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

import frontend.Task
import frontend.domain.model.*
import frontend.infrastructure.*
import frontend.infrastructure.interpreter.AppStateActionsInterpreter
import frontend.view.components.*

@JSImport("Sources/css/style.css", JSImport.Default)
@js.native
object assets extends js.Object

@js.native
@JSImport("md5", JSImport.Namespace)
object MD5 extends js.Object:
  def apply(value: String): String = js.native

object Main:
  import japgolly.scalajs.react.*
  import japgolly.scalajs.react.vdom.all.*
  import japgolly.scalajs.react.ReactMonocle.*
  import japgolly.scalajs.react.extra.StateSnapshot
  import monocle.*

  // val NameChanger = ScalaComponent
  //   .builder[StateSnapshot[String]]("Name changer")
  //   .render_P { ss =>
  //     def updateName = (event: ReactEventFromInput) => ss.setState(event.target.value)

  //     input.text(value := ss.value, onChange ==> updateName)
  //   }
  //   .build

  // final case class Name(firstName: String, surname: String)

  // val Main = ScalaComponent
  //   .builder[Unit]
  //   .initialState[Name](Name("John", "Wick"))
  //   .render { $ =>
  //     val name = $.state
  //     val firstNameV = StateSnapshot.zoomL(Focus[Name](_.firstName)).of($)
  //     val surnameV = StateSnapshot.zoomL(Focus[Name](_.surname)).of($)

  //     val rofl =
  //       IO.pure("New name").flatMap(newName => $.modStateAsync(_.copy(firstName = newName)))

  //     div(
  //       label("First name:", NameChanger(firstNameV)),
  //       label("Surname:", NameChanger(surnameV)),
  //       button(
  //         onClick --> rofl,
  //         "Set new name async!"
  //       ),
  //       Component(),
  //       p(s"My name is ${name.surname}, ${name.firstName} ${name.surname}.")
  //     )
  //   }
  //   .componentDidMount(c => c.modStateAsync(a => a.copy(surname = "Loaded")))
  //   .build
  //

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
        Button("Click me", Seq(onClick --> count.modState(_ + 1)))(),
        p(s"Your favourite fruit is a ${fruit.value}!")
      )
    )

object ReactApp extends IOApp.Simple:
  def require(): Unit =
    assets
  def run: IO[Unit] = myProgram.useForever

  def myProgram: Resource[IO, Unit] =
    require()

    import shared.models.*
    import frontend.infrastructure.*

    val requests = Requests[IO]
    val appState = new AppStateActionsInterpreter(requests)

    import japgolly.scalajs.react.vdom.all.*
    import org.scalajs.dom.document

    def message(msg: String)(using pos: org.tpolecat.sourcepos.SourcePos) =
      s"$msg from: $pos"

    for {
      phoneBook <- Tasks.playingSetup.toResource
      world <- WorldImpl(phoneBook, WorldSpeed.RealTime)

      suite = new Task(world)

      task = new SimpleIOSuite:
        override def maxParallelism = 1

        loggedTest("Introduction") { log =>
          resultFor("introduction") {
            suite.introduction(log)
          }
        }

        loggedTest("Task 1") { log =>
          resultFor("1") {
            ignore("Not Yet")
          }
        }

        // Error handling
        loggedTest("Task 2") { log =>
          /* Test ideas:
           *
           *
           *
           * Call 100 people in range 7000-8000 at the same time and get the
           * total sum they can give
           *   -> traverse vs parTraverse
           *
           *
           *
           * */
          resultFor("2") {
            suite.task2(log)
          }
        }

        loggedTest("Task 3") { log =>
          resultFor("3") {
            suite.task3(log)
          }
        }

        def resultFor(task: String)(body: => IO[String]): IO[Expectations] =
          body.map(validate(task, _))

        def validate(task: String, result: String): Expectations =
          val key = task match {
            case "introduction" => "9fa0416b6d65fa74be1eace094de8a2c"
            case "1" => "9fa0416b6d65fa74be1eace094de8a2c"
            case "2" => "a1d0c6e83f027327d8461063f4ac58a6"
            case "3" => "9fa0416b6d65fa74be1eace094de8a2c"
          }

          val res = MD5(result)

          expect.eql(key, res)

      root = div(
        h1(s"Hello ${message("there")}", className := "bg-red-900 text-white"),
        div(className := "grid grid-cols-2 divide-x divide-green-500",
            SpecComponent(task),
            WorldComponent(world)
        )
      )

      _ <- IO.delay(root.renderIntoDOM(document.getElementById("root"))).toResource
    } yield ()
