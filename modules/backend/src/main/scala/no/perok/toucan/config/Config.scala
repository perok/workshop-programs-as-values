package no.perok.toucan.config

import cats.effect._
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

case class Server(port: Int)
case class DB(name: String, user: String, password: String)

case class Config(environment: String, tokenSecret: String, server: Server, db: DB)

object Config {
  def apply[F[_]: Sync: ContextShift](blocker: Blocker): F[Config] =
    ConfigSource.default.at("toucan").loadF[F, Config](blocker)
}
