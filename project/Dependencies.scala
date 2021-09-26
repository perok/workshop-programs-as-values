import sbt._
//import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  val http4sVersion = "0.22.5"
  val doobieVersion = "0.13.4"
  val circeVersion = "0.14.1"
  // val scalaJsReact = "1.4.2"
  val monocleVersion = "2.1.0"
  val fs2Version = "2.5.9"

  val backendDependencies: Seq[ModuleID] = {
    val httpServer: Seq[ModuleID] = Seq(
      "org.http4s" %% "http4s-dsl",
      "org.http4s" %% "http4s-circe",
      "org.http4s" %% "http4s-blaze-server",
      "org.http4s" %% "http4s-blaze-client"
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
      // TODO mdoc
      "org.scalatest" %% "scalatest" % "3.2.10",
      "org.tpolecat" %% "doobie-scalatest" % doobieVersion
    ).map(_ % "it,test")

    val utils = Seq(
      "org.typelevel" %% "mouse" % "1.0.4",
      "io.chrisdavenport" %% "log4cats-core" % "1.1.1",
      "io.chrisdavenport" %% "log4cats-slf4j" % "1.1.1",
      "ch.qos.logback" % "logback-classic" % "1.2.6",
      "ch.qos.logback" % "logback-core" % "1.2.6",
      // TODO ciris
      // "com.github.pureconfig" %% "pureconfig-generic" % "0.13.0",
      // "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.13.0",
      "com.lihaoyi" %% "pprint" % "0.6.6"
    )

    httpServer ++ database ++ crypto ++ utils ++ testLibs
  }.map(_.excludeAll(ExclusionRule(organization = "ch.qos.logback"))) // Hvorfor? :P TODO
}
