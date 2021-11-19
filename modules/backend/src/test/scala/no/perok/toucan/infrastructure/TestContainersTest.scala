package test
// package morespecific TODO structure like this
import cats.effect.*

object test {
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
}
