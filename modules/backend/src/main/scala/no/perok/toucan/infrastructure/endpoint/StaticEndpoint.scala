package no.perok.toucan.infrastructure.endpoint

import cats.syntax.option._
import cats.effect._
import org.http4s._
import org.http4s.dsl.Http4sDsl
// import scala.concurrent.ExecutionContext.Implicits.global

class StaticEndpoint[F[_]: Sync: ContextShift](blocker: Blocker) extends Http4sDsl[F] {
  def static(file: String, request: Request[F]): F[Response[F]] =
    StaticFile.fromResource("/" + file, blocker, request.some).getOrElseF(NotFound())

  def bundleUrl(projectName: String): Option[String] = {
    val name = projectName.toLowerCase
    Seq(s"$name-opt-bundle.js", s"$name-fastopt-bundle.js")
      .find(name => getClass.getResource(s"/public/$name") != null)
  }
//  logger.info(bundleUrl("frontend").toString)

  val supportedFiles = List(".js", ".css", ".map", ".html", ".webm")

  def endpoints: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case request @ GET -> Root / path if supportedFiles.exists(path.endsWith) =>
        static(path, request)
    }
}

object StaticEndpoint {
  def endpoints[F[_]: Sync: ContextShift](blocker: Blocker): HttpRoutes[F] =
    new StaticEndpoint[F](blocker).endpoints
}
