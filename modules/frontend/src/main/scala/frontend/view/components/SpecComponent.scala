package frontend.view.components

import cats.*
import cats.effect.*
import cats.effect.std.*
import cats.syntax.all.*
import fs2.Stream
import japgolly.scalajs.react.ReactMonocle.*
import japgolly.scalajs.react.*
import japgolly.scalajs.react.extra.StateSnapshot
import japgolly.scalajs.react.vdom.all.*
import monocle.*
import weaver.*

import scala.concurrent.duration.*
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("ansi-to-html", JSImport.Namespace)
@js.native
class Convert extends js.Object {
  def toHtml(in: String, newline: Boolean): String = js.native
}

val convert = new Convert

val SingleTestComponent: ScalaFnComponent.Component[TestOutcome, CtorType.Props] =
  ScalaFnComponent[TestOutcome](outcome =>
    div(
      className := "text-base truncate",
      dangerouslySetInnerHtml := s"<pre>${convert.toHtml(outcome.formatted(TestOutcome.Verbose), true)}</pre>"
    )
  )

// TODO add flip between verbosity
val SpecComponent: ScalaFnComponent.Component[EffectSuite[IO], CtorType.Props] =
  ScalaFnComponent
    .withHooks[EffectSuite[IO]]
    .useState(List.empty[TestOutcome])
    .useState(false)
    .useEffectOnMountBy((spec, result, running) =>
      running.setState(true).to[IO] >>
        spec
          .spec(List.empty)
          .evalMap(newS => result.modState(_.appended(newS)).to[IO])
          .onFinalize(running.setState(false).to[IO])
          .compile
          .drain
          .void
    )
    .render((spec, result, running) =>
      div(
        classSet1("border border-blue-300 shadow rounded-md p-4 m-10",
                  "animate-pulse" -> running.value
        ),
        h3(spec.name, className := "text-lg underline"),
        ul(
          result.value.toVdomArray { outcome =>
            SingleTestComponent.withKey(outcome.name)(outcome)
          }
        )
      )
    )
