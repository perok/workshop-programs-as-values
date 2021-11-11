import sbt._
//import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  val http4sVersion = "0.23.6"
  val tapirVersion = "0.19.0-M14"
  val doobieVersion = "1.0.0-RC1"
  val circeVersion = "0.14.1"
  val scalaJsReact = "2.0.0"
  val monocleVersion = "3.1.0"
  val fs2Version = "3.2.2"
  val testcontainersScalaVersion = "0.39.11"

  val backendDependencies: Seq[ModuleID] = {
    val httpServer: Seq[ModuleID] = Seq(
      "org.http4s" %% "http4s-dsl",
      "org.http4s" %% "http4s-circe",
      "org.http4s" %% "http4s-ember-server",
      "org.http4s" %% "http4s-ember-client"
    ).map(_ % http4sVersion) ++ Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-redoc" % tapirVersion
    )

    val database: Seq[ModuleID] = Seq(
      // TODO https://github.com/tpolecat/skunk
      "org.tpolecat" %% "doobie-core",
      "org.tpolecat" %% "doobie-hikari",
      "org.tpolecat" %% "doobie-postgres"
    ).map(_ % doobieVersion) ++
      Seq("org.flywaydb" % "flyway-core" % "8.0.2")

    val crypto: Seq[ModuleID] = Seq(
      "com.github.jwt-scala" %% "jwt-circe" % "9.0.2",
      "org.mindrot" % "jbcrypt" % "0.4"
    )

    val testLibs: Seq[ModuleID] = Seq(
      // TODO weaver
      "org.scalameta" %% "munit" % "0.7.29",
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.6",
      "org.tpolecat" %% "doobie-munit" % doobieVersion,
      "com.dimafeng" %% "testcontainers-scala-munit" % testcontainersScalaVersion,
      "com.dimafeng" %% "testcontainers-scala-postgresql" % testcontainersScalaVersion
    ).map(_ % "it,test")

    val utils = Seq(
      "is.cir" %% "ciris" % "2.2.0",
      "org.typelevel" %% "mouse" % "1.0.7",
      "org.typelevel" %% "log4cats-slf4j" % "2.1.1",
      "ch.qos.logback" % "logback-classic" % "1.2.6",
      "ch.qos.logback" % "logback-core" % "1.2.6",
      "com.lihaoyi" %% "pprint" % "0.6.6"
    )

    httpServer ++ database ++ crypto ++ utils ++ testLibs
  }.map(_.excludeAll(ExclusionRule(organization = "ch.qos.logback"))) // Hvorfor? :P TODO
}
