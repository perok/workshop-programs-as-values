package backend.config

import ciris.*

final case class DB(name: String, user: String, password: String, port: Int = 5432):
  // def jdbcUrl = s"jdbc:postgresql:$name"
  def jdbcUrl = s"jdbc:postgresql://localhost:$port/$name"

final case class Config(environment: String, tokenSecret: String, db: DB)

object Config:
  val load = env("APP_ENV")
    .as[String]
    .map(env => Config(env, "", DB("postgres", "postgres", "mysecretpassword")))
