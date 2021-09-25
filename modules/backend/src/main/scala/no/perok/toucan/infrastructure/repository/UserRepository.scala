package no.perok.toucan.infrastructure.repository

import doobie._
import doobie.postgres._
import doobie.implicits._
import doobie.postgres.implicits._
import no.perok.toucan.domain.models._
import no.perok.toucan.infrastructure.CryptographyLogic

object UserRepository {
  def getUserHashByUsername(username: String): ConnectionIO[Option[String]] =
    Statements.getUserHashByUsername(username).option

  def getUserByUsername(username: String): ConnectionIO[Option[WithId[User]]] =
    Statements.getUser(username).option

  def getUserById(id: ID[User]): ConnectionIO[Option[User]] =
    Statements.getUser(id).option

  def addUser(newUser: NewUserForm): ConnectionIO[String Either Int] =
    Statements
      .addUser(
        newUser
          .copy(password = CryptographyLogic.hashPassword(newUser.password))
      )
      .run
      .attemptSomeSqlState {
        case sqlstate.class23.UNIQUE_VIOLATION => s"User ${newUser.username} already exists"
      }

  object Statements {
    val listUsers: Query0[User] =
      sql"select email, username, created from user_account"
        .query[User]

    def getUser(username: String): Query0[WithId[User]] =
      sql"select id, email, username, created from user_account where username = $username"
        .query[WithId[User]]

    def getUser(id: ID[User]): Query0[User] =
      sql"select email, username, created from user_account where id = $id"
        .query[User]

    def getUserHashByUsername(username: String): Query0[String] =
      sql"select password from user_account where username = $username"
        .query[String]

    def addUser(newUser: NewUserForm): Update0 =
      sql"""
           |INSERT INTO user_account
           |  (username, password, email)
           |VALUES
           |  (${newUser.username}, ${newUser.password}, ${newUser.email})
         """.stripMargin.update
  }
}
