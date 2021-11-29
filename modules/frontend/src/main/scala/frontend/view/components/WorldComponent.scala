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

import frontend.domain.model.*
import frontend.view.components.*

given Reusability[Map[Unique.Token, PhoneState]] = Reusability.by_==
given Reusability[World] = Reusability.byRef
given Reusability[PhoneState] = Reusability.by_==

val PhoneComponent: ScalaFnComponent.Component[PhoneState, CtorType.Props] =
  ScalaFnComponent
    .withHooks[PhoneState]
    .useState(Option.empty[String])
    .renderWithReuse { (phone, sayingA) =>
      val text = phone match {
        case PhoneState.Available => "available"
        case PhoneState.Dialing(who) => s"dialing $who"
        case PhoneState.Calling(who) => s"calling $who"
        case PhoneState.Talking(who, _, _) => s"talking ${who.name}"
        case PhoneState.Hangup => "hangup"
      }
      val spinning = phone match {
        case PhoneState.Dialing(_) => true
        case _ => false
      }

      val image = phone match {
        case PhoneState.Talking(who, _, _) => who.image.some
        case _ => Option.empty
      }

      val saying = phone match {
        case PhoneState.Talking(_, saying, _) => saying
        case _ => None
      }

      val playerSaying = phone match {
        case PhoneState.Talking(_, _, saying) => saying
        case _ => None
      }

      li(
        className := "flex flex-row h-32 space-x-10",
        div(className := "w-32",
            playerSaying
              .map(tthis => div(className := "speech-bubble", h2(className := "p-2", tthis)))
              .whenDefined
        ),
        div(
          className := "flex-column text-center",
          phonedIcon(spinning),
          text
        ),
        image.map(i => img(className := "object-contain", src := i)).whenDefined,
        saying.map { tthis =>
          val clazzName = tthis match {
            case BotSaying.Text(text) =>
              h2(text)
              "speech-bubble-readable"
            case BotSaying.TextJustShow(text) =>
              h2(text)
              "speech-bubble"
            case BotSaying.TextWithConfirmation(text, callback) =>
              "speech-bubble"
          }
          div(
            className := clazzName, // h-10
            div(
              className := "p-2",
              tthis match {
                case BotSaying.Text(text) => h2(text)
                // TODO different colors
                case BotSaying.TextJustShow(text) => h2(text)
                case BotSaying.TextWithConfirmation(text, callback) =>
                  div(
                    h2(text),
                    Button("Ok", Seq(onClick --> callback.complete(()).void))()
                  )
              }
            )
          )
        }.whenDefined
      )
    }

val WorldComponent: ScalaFnComponent.Component[World, CtorType.Props] =
  ScalaFnComponent
    .withHooks[World]
    .useState(Map.empty[Unique.Token, PhoneState])
    .useEffectOnMountBy((world, state) =>
      world.worldState.discrete
        .evalMapChunk(newS => state.setState(newS).to[IO])
        .compile
        .drain
        .void
    )
    .renderWithReuse((props, state) =>
      div(className := "p-10",
          ul(className := "flex-row",
             state.value.toVdomArray { (id, phone) =>
               PhoneComponent.withKey(id.hash)(phone)
             }
          )
      )
    )
