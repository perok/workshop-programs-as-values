package no.perok.toucan.infrastructure.repository

import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import cats.effect._
import doobie._
import no.perok.toucan.config.{Config, DBConfig}
import no.perok.toucan.domain.models._
import doobie.scalatest.IOChecker
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import scala.concurrent.ExecutionContext.Implicits.global

// TODO Transactor with Task. This is a test of type IT
class TroopDAOSpec extends AnyFunSuite with Matchers with IOChecker {

  implicit def localLogger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]
  implicit val cs = IO.contextShift(global)
  val blocker = Blocker.liftExecutionContext(global)

  val settings: Config = Config[IO](blocker).unsafeRunSync()
  val transactor: Transactor[IO] =
    DBConfig.getXA[IO](settings.db, dropFirst = false).unsafeRunSync()

  val troopId: ID[Troop] = ID[Troop](0)
  val userId: ID[User] = ID[User](0)

  test("listTroops") { check(TroopRepository.Statements.listTroops) }
  test("getTroop") { check(TroopRepository.Statements.getTroop(troopId)) }
  test("updateTroops") { check(TroopRepository.Statements.updateTroop(troopId, TroopForm(""))) }
  test("insertTroops") { check(TroopRepository.Statements.insertTroop(TroopForm(""))) }
  test("add user to troop") {
    check(UserInTroopRepository.Statements.addToTroop(troopId, userId, isAdmin = false))
  }
}
