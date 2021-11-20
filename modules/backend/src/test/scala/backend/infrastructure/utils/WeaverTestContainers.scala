package backend.infrastructure.utils

import cats.effect.*
import cats.syntax.all.*
import com.dimafeng.testcontainers.*

object WeaverTestContainers:
  def containerResource[F[_]: Sync](
      container: ContainerDef
  ): Resource[F, container.Container] =
    // TODO this does not handle multiple containers?
    // https://github.com/testcontainers/testcontainers-scala#multiple-containers-in-tests
    Resource.make(Sync[F].blocking(container.start()))(createdContainer =>
      Sync[F].blocking(createdContainer.stop())
    )
