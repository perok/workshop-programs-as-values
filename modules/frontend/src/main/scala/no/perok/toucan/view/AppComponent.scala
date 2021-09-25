package no.perok.toucan.view

import cats._
import cats.implicits._
import no.perok.toucan.domain.model._
import no.perok.toucan.domain.model.moviedb._
import no.perok.toucan.Connector
import no.perok.toucan.Store
import slinky.web.html._
import no.perok.toucan.domain.algebra._
import slinky.core.WithAttrs
import slinky.core.facade.ReactElement
import com.olegpy.shironeko.interop._

class AppComponent[F[_]: Monad](appStateAgebra: AppStateActionsAlgebra[F])
    extends Connector.ContainerNoProps {
  type State = AppState
  override def subscribe[FF[_]: Subscribe] = getAlgebra.main.discrete
  override def render[FF[_]: Render](state: State) = {

    val troopView: Option[ReactElement] = for {
      user <- state.user
      currentTroopId <- state.navigationState.currentTroop
      troop <- user.troops.find(_.id === currentTroopId)
      // TODO onClick og testCounter viser at state blir overskrevet.. Kan BackendScope hjelpe?
    } yield {
      // val voter: MovieInTroopId => Option[Boolean] => (() => Unit) =
      //   id => vote => toCallback(appStateAgebra.voteOn(id, vote).as(()))

      // val troopComponent =
      // TroopComponent(troop, voter).render
      println(troop)

      div(NavBar.lol) //, troopComponent)
    }

    troopView.getOrElse(div("No Troop or User found"))
  }
}
