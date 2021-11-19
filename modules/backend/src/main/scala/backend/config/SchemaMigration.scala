package backend.config

import cats.effect.*
import cats.syntax.all.*
import org.flywaydb.core.Flyway
import org.typelevel.log4cats.Logger

object SchemaMigration:
  implicit def localLogger[F[_]: Sync]: Logger[F] =
    org.typelevel.log4cats.slf4j.Slf4jLogger.getLogger

  def migrate[F[_]: Sync](db: DB, dropFirst: Boolean): F[Unit] =
    Sync[F].delay {
      val flyway = Flyway
        .configure()
        .dataSource(db.jdbcUrl, db.user, db.password)
        .load

      if (dropFirst) then flyway.clean()

      flyway.migrate()
      ()
    } *> Logger[F].info("DB migration complete")
