package backend

import cats.effect.*
import com.dimafeng.testcontainers._
import natchez.Trace.Implicits.noop
import org.testcontainers.lifecycle.Startable
import org.testcontainers.utility.DockerImageName
import skunk.Session
import weaver.*

import backend.config.*
import backend.infrastructure.repository.*

object TestDb extends IOSuite:
  override type Res = Session[IO]
  override def sharedResource: Resource[IO, Session[IO]] =
    WeaverTestContainers
      .containerResource(
        PostgreSQLContainer.Def(dockerImageName = DockerImageName.parse("postgres:14.1"))
      )
      .map(container =>
        DB(container.container.getDatabaseName,
           container.container.getUsername,
           container.container.getPassword,
           container.mappedPort(5432)
        )
      )
      .evalTap(settings => SchemaMigration.migrate(settings, dropFirst = true))
      .flatMap(settings =>
        Session.single(host = "localhost",
                       port = settings.port,
                       user = settings.user,
                       database = settings.name,
                       password = Some(settings.password)
        )
      )

  test("Can say hello") { session =>
    for {
      helloResponse <- session.option(HelloRepository.getHello)
    } yield expect(helloResponse.isEmpty)
  }

object WeaverTestContainers:
  import com.dimafeng.testcontainers.*

  def containerResource[F[_]: Sync](
      container: ContainerDef
  ): Resource[F, container.Container] =
    // TODO this does not handle multiple containers?
    // https://github.com/testcontainers/testcontainers-scala#multiple-containers-in-tests
    Resource.make(Sync[F].blocking(container.start()))(createdContainer =>
      Sync[F].blocking(createdContainer.stop())
    )

  val result = containerResource[IO](PostgreSQLContainer.Def())
