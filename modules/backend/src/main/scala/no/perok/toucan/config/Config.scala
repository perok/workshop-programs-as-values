package no.perok.toucan.config

import ciris.*
import com.comcast.ip4s.*

final case class Server(port: Port)
final case class DB(name: String, user: String, password: String):
  // def jdbcUrl = s"jdbc:postgresql:$name"
  def jdbcUrl = s"jdbc:postgresql://localhost:5432/$name"

final case class Config(environment: String, tokenSecret: String, server: Server, db: DB)

object Config:
  val load = env("APP_ENV")
    .as[String]
    .map(env => Config(env, "", Server(port"8081"), DB("postgres", "postgres", "mysecretpassword")))
