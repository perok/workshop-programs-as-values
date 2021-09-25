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
class UserDAOSpec extends AnyFunSuite with Matchers with IOChecker {
  implicit def localLogger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]
  implicit val cs = IO.contextShift(global)
  val blocker = Blocker.liftExecutionContext(global)

  val settings: Config = Config[IO](blocker).unsafeRunSync()
  val transactor: Transactor[IO] = // TODO ikkje kjøre migrering her
    DBConfig.getXA[IO](settings.db, dropFirst = false).unsafeRunSync()

  val newUser: NewUserForm = NewUserForm("batman", "guesswho", "bat@man.no")

  test("listUsers") { check(UserRepository.Statements.listUsers) }
  test("getUser") { check(UserRepository.Statements.getUser(ID[User](0))) }
  test("addUser") { check(UserRepository.Statements.addUser(newUser)) }
}
