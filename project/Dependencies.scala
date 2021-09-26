import sbt._
//import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  val http4sVersion = "0.23.4"
  val doobieVersion = "1.0.0-RC1"
  val circeVersion = "0.14.1"
  // val scalaJsReact = "1.4.2"
  val monocleVersion = "3.1.0"
  val fs2Version = "3.1.3"
  val testcontainersScalaVersion = "0.39.8"

  val backendDependencies: Seq[ModuleID] = {
    val httpServer: Seq[ModuleID] = Seq(
      "org.http4s" %% "http4s-dsl",
      "org.http4s" %% "http4s-circe",
      "org.http4s" %% "http4s-ember-server",
      "org.http4s" %% "http4s-ember-client"
    ).map(_ % http4sVersion)

    val database: Seq[ModuleID] = Seq(
      "org.tpolecat" %% "doobie-core",
      "org.tpolecat" %% "doobie-postgres"
    ).map(_ % doobieVersion) ++
      Seq("org.flywaydb" % "flyway-core" % "7.15.0")

    val crypto: Seq[ModuleID] = Seq(
      "com.github.jwt-scala" %% "jwt-circe" % "9.0.1",
      "org.mindrot" % "jbcrypt" % "0.4"
    )

    val testLibs: Seq[ModuleID] = Seq(
      "org.scalameta" %% "munit" % "0.7.29",
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.5",
      "org.tpolecat" %% "doobie-munit" % doobieVersion,
      "com.dimafeng" %% "testcontainers-scala-munit" % testcontainersScalaVersion,
      "com.dimafeng" %% "testcontainers-scala-postgresql" % testcontainersScalaVersion
    ).map(_ % "it,test")

    val utils = Seq(
      "is.cir" %% "ciris" % "2.1.1",
      "org.typelevel" %% "mouse" % "1.0.4",
      "org.typelevel" %% "log4cats-slf4j" % "2.1.1",
      "ch.qos.logback" % "logback-classic" % "1.2.6",
      "ch.qos.logback" % "logback-core" % "1.2.6",
      "com.lihaoyi" %% "pprint" % "0.6.6"
    )

    httpServer ++ database ++ crypto ++ utils ++ testLibs
  }.map(_.excludeAll(ExclusionRule(organization = "ch.qos.logback"))) // Hvorfor? :P TODO
}
