package no.perok.toucan.config

import cats.effect._
import cats.syntax.all._
import doobie._
import org.typelevel.log4cats.Logger

object DBConfig:
  def getXA[F[_]: Async: Logger](settings: DB, dropFirst: Boolean): F[Transactor[F]] =
    SchemaMigration
      .migrate[F](settings, dropFirst)
      .map(_ =>
        Transactor.fromDriverManager[F](
          driver = "org.postgresql.Driver",
          url = s"jdbc:postgresql:${settings.name}",
          user = settings.user,
          pass = settings.password
        )
      )
