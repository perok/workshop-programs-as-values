package no.perok.toucan.infrastructure.endpoint

import java.time.LocalDateTime

import cats.data._
import cats.syntax.show._
import cats.syntax.option._
import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import no.perok.toucan.domain.models._
import no.perok.toucan.domain.algebras.{TroopAlgebra, VoteAlgebra}
import no.perok.toucan.domain.TroopProgram
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TroopServiceSpec extends AnyFlatSpec with Matchers {
  //val xa =
  //  Transactor.fromConnection[IOLite](null).copy(strategy0 = Strategy.void)
  // TODO Heller kjøre white box tester på servicer?
  // This is only tests of the HTTP4S interface.
  // Thoroughly test return code and data given certain scenarios

  implicit val troopEntityDecoder: EntityDecoder[IO, Troop] =
    org.http4s.circe.jsonOf[IO, Troop]

  val testUser = WithId(ID[User](0), User("", "", LocalDateTime.now()))
  val testTroop = WithId(ID[Troop](0), Troop("The Papricorns"))

  val troopAlgebra: TroopAlgebra[IO] = new TroopAlgebra[IO] {
    def listTroops: IO[List[Troop]] = ???
    def listTroopsFor(user: ID[User]): IO[List[UserInTroop]] = ???
    def getTroop(id: ID[Troop]): IO[Option[Troop]] = {
      IO.pure(if (id === testTroop.id) testTroop.model.some else None)
    }
    def updateTroop(id: ID[Troop], newTroop: TroopForm): IO[Troop] = ???
    def insertTroop(newTroop: TroopForm): IO[Either[ExpectedError, WithId[Troop]]] = ???
    def insertMovie(troopId: ID[Troop], movieId: ID[Movie]): IO[ID[MovieInTroop]] =
      ???
    def addUserToTroop(
        troop: ID[Troop],
        user: ID[User],
        isAdmin: Boolean
    ): IO[Either[ExpectedError, ID[UserInTroop]]] = ???
  }

  val voteAlgebra: VoteAlgebra[IO] = new VoteAlgebra[IO] {
    def getAllVotesFor(movieInTroop: ID[MovieInTroop]): IO[List[Vote]] = ???
    def setVote(
        troop: ID[Troop],
        movie: ID[Movie],
        user: ID[User],
        positive: Boolean
    ): IO[Option[ID[Vote]]] = ???
  }

  val troopService: HttpRoutes[IO] = {
    val handler = new TroopProgram[IO](troopAlgebra, voteAlgebra)

    val service: Kleisli[OptionT[IO, ?], AuthedRequest[IO, WithId[User]], Response[IO]] =
      new TroopEndpoint(handler, troopAlgebra, voteAlgebra).endpoints

    val authUser: Kleisli[OptionT[IO, ?], Request[IO], AuthedRequest[IO, WithId[User]]] =
      Kleisli(a => OptionT.liftF(IO.pure(AuthedRequest(testUser, a))))

    // Skip the authentication middleware
    service.compose(authUser)
  }

  "The troop service should" should "GET /api/troop/{id}" in {
    val myReq =
      Request[IO](Method.GET, Uri.unsafeFromString(show"/troop/${testTroop.id}"))

    val result: Response[IO] =
      troopService.orNotFound.run(myReq).unsafeRunSync()

    val foundTroop: Troop = result.as[Troop].unsafeRunSync()

    result.status shouldEqual Status.Ok
//    foundTroop.id shouldEqual testTroop.id
    foundTroop.name shouldEqual testTroop.model.name
  }
}
