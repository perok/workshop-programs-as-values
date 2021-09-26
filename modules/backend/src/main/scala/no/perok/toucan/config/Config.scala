package no.perok.toucan.config

import ciris._
import com.comcast.ip4s.Port

case class Server(port: Port)
case class DB(name: String, user: String, password: String)

case class Config(environment: String, tokenSecret: String, server: Server, db: DB)

object Config {
  val config = env("APP_ENV")
    .as[String]
    .map(env => Config(env, "", Server(Port.fromInt(1).get), DB("", "", "")))
}
