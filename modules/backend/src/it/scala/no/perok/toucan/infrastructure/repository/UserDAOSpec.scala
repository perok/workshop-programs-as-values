// package no.perok.toucan.infrastructure.repository

// import munit.*
// import org.typelevel.log4cats.Logger
// import org.typelevel.log4cats.slf4j.Slf4jLogger
// import cats.effect.*
// import doobie.*
// import no.perok.toucan.domain.models.*
// import com.dimafeng.testcontainers.PostgreSQLContainer
// import com.dimafeng.testcontainers.munit.TestContainerForAll

// // TODO Transactor with Task. This is a test of type IT
// class UserDAOSpec extends FunSuite with TestContainerForAll with doobie.munit.IOChecker {
//   override val containerDef: PostgreSQLContainer.Def = PostgreSQLContainer.Def()

//   val transactor: Transactor[IO] =
//     // TODO kjøre migrering her
//     withContainers { postgresContainer =>
//       Transactor.fromDriverManager[IO](
//         "org.postgresql.Driver",
//         postgresContainer.jdbcUrl,
//         postgresContainer.username,
//         postgresContainer.password
//       )
//     }

//   val newUser: NewUserForm = NewUserForm("batman", "guesswho", "bat@man.no")

//   test("listUsers") { check(UserRepository.Statements.listUsers) }
//   test("getUser") { check(UserRepository.Statements.getUser(ID[User](0))) }
//   test("addUser") { check(UserRepository.Statements.addUser(newUser)) }
// }
