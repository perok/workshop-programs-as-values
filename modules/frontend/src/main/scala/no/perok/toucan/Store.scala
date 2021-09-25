package no.perok.toucan

import com.olegpy.shironeko._
import cats.effect._
import cats.implicits._
import no.perok.toucan.domain.model.AppState
import no.perok.toucan.domain.model.NavigationState

class Store[F[_]](dsl: StoreDSL[F]) {
  import dsl._
  val abc: AppState = AppState(None, NavigationState(None), Map.empty)

  val counter: Cell[F, Int] = cell(0)
  val changes: Cell[F, Int] = cell(0)
  val main: Cell[F, AppState] = cell(abc)
}
object Store {
  def make[F[_]: Concurrent]: F[Store[F]] =
    StoreDSL[F].use(new Store[F](_).pure[F])
}
