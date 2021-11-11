package no.perok.toucan.config

import cats.effect._
import cats.syntax.all._
import org.flywaydb.core.Flyway
import org.typelevel.log4cats.Logger

private[config] object SchemaMigration {
  implicit def localLogger[F[_]: Sync]: Logger[F] =
    org.typelevel.log4cats.slf4j.Slf4jLogger.getLogger

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def migrate[F[_]: Sync](db: DB, dropFirst: Boolean): F[Unit] =
    Sync[F].delay {
      val flyway = Flyway
        .configure()
        .dataSource(db.jdbcUrl, db.user, db.password)
        .load

      if (dropFirst) {
        flyway.clean()
      }
      flyway.baseline()
      flyway.migrate()
      ()
    } *> Logger[F].info("DB migration complete")
}
