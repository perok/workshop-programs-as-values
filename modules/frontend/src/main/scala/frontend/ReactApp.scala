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
import frontend.view.components.*
import frontend.infrastructure.*

@JSImport("@css-sources/style.css", JSImport.Default)
@js.native
object assets extends js.Object

@js.native
@JSImport("md5", JSImport.Namespace)
object MD5 extends js.Object:
  def apply(value: String): String = js.native

object ReactApp extends IOApp.Simple:
  import org.scalajs.dom.document
  import japgolly.scalajs.react.*
  import japgolly.scalajs.react.vdom.all.*

  def require(): Unit =
    assets
  def run: IO[Unit] = myProgram.useForever

  def myProgram: Resource[IO, Unit] =
    require()

    for {
      phoneBook <- Tasks.playingSetup.toResource
      world <- WorldImpl(phoneBook, WorldSpeed.RealTime)

      suite = new Task(world):
        override def name: String = "Programs as values"

        override def maxParallelism = 1

        loggedTest("Introduction") { log =>
          resultFor("introduction") {
            introduction(log)
          }
        }

        loggedTest("Task 1 - Handling errors") { log =>
          resultFor("1") {
            task1(log)
          }
        }

        loggedTest("Task 2 - Traverse") { log =>
          resultFor("2") {
            task2(log)
          }
        }

        def resultFor(task: String)(body: => IO[String]): IO[Expectations] =
          body.map(validate(task, _))

        def validate(task: String, result: String): Expectations =
          val key = task match {
            case "introduction" => "9fa0416b6d65fa74be1eace094de8a2c"
            case "1" => "9fa0416b6d65fa74be1eace094de8a2c"
            case "2" => "f103399687b5252b2569fb6098d3c529"
          }

          val res = MD5(result)

          expect.eql(key, res)

      root = div(
        div(className := "grid grid-cols-2 divide-x divide-green-500",
            SpecComponent(suite),
            WorldComponent(world)
        )
      )

      _ <- IO.delay(root.renderIntoDOM(document.getElementById("root"))).toResource
    } yield ()
