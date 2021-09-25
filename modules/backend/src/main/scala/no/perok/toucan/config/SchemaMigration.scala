package no.perok.toucan.config

import io.chrisdavenport.log4cats.Logger
// import io.chrisdavenport.log4cats.scribe.ScribeLogger
import cats.effect._
import cats.implicits._
import org.flywaydb.core.Flyway

private[config] object SchemaMigration {
  // implicit def localLogger[F[_]: Sync]: Logger[F] = ScribeLogger.empty[F]

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def migrate[F[_]: Sync: Logger](db: DB, dropFirst: Boolean): F[Unit] =
    Sync[F].delay {
      val flyway = Flyway
        .configure()
        .dataSource(db.name, db.user, db.password)
        .load

      if (dropFirst) {
        flyway.clean()
      }
      flyway.baseline()
      flyway.migrate()
      ()
    } *> Logger[F].info("DB migration complete")
}
