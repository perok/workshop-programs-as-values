package no.perok.toucan.config

import io.chrisdavenport.log4cats.Logger
import cats.effect._
import cats.syntax.functor._
import doobie._

object DBConfig {
  def getXA[F[_]: Async: ContextShift: Logger](settings: DB, dropFirst: Boolean): F[Transactor[F]] =
    SchemaMigration
      .migrate[F](settings, dropFirst)
      .map(
        _ =>
          Transactor.fromDriverManager[F](
            driver = "org.postgresql.Driver",
            url = s"jdbc:postgresql:${settings.name}",
            user = settings.user,
            pass = settings.password
        ))
}
