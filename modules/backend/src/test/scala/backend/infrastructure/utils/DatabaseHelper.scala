package backend.infrastructure.utils

import cats.effect.*
import cats.effect.std.*
import cats.syntax.all.*
import com.dimafeng.testcontainers.*
import fs2.io.net.Network
import natchez.Trace.Implicits.noop
import org.testcontainers.utility.DockerImageName
import skunk.Session

import backend.config.{DB, SchemaMigration}

object DatabaseHelper:
  def postgres[F[_]: Async: Console] =
    WeaverTestContainers
      .containerResource[F](
        PostgreSQLContainer.Def(dockerImageName = DockerImageName.parse("postgres:14.1"))
      )
      .flatMap(DatabaseHelper.containerToSession)

  def containerToSession[F[_]: Async: Console](
      container: PostgreSQLContainer
  ): Resource[F, Session[F]] =
    val settings =
      DB(container.container.getDatabaseName,
         container.container.getUsername,
         container.container.getPassword,
         container.mappedPort(5432)
      )

    Resource.eval(SchemaMigration.migrate[F](settings, dropFirst = true)) >>
      Session.single[F](host = "localhost",
                        port = settings.port,
                        user = settings.user,
                        database = settings.name,
                        password = Some(settings.password)
      )
