import sbt._
//import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  val http4sVersion = "0.21.8"
  val doobieVersion = "0.9.2"
  val circeVersion = "0.13.0"
  // val scalaJsReact = "1.4.2"
  val monocleVersion = "2.1.0"
  val fs2Version = "2.4.4"

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
      Seq("org.flywaydb" % "flyway-core" % "6.5.7")

    val crypto: Seq[ModuleID] = Seq(
      "com.pauldijou" %% "jwt-core" % "4.3.0",
      "com.pauldijou" %% "jwt-circe" % "4.3.0",
      "org.mindrot" % "jbcrypt" % "0.4"
    )

    val testLibs: Seq[ModuleID] = Seq(
      "org.scalatest" %% "scalatest" % "3.1.4",
      "org.tpolecat" %% "doobie-scalatest" % doobieVersion
    ).map(_ % "it,test")

    val utils = Seq(
      "org.typelevel" %% "mouse" % "0.25",
      "io.chrisdavenport" %% "log4cats-core" % "1.1.1",
      "io.chrisdavenport" %% "log4cats-slf4j" % "1.1.1",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "ch.qos.logback" % "logback-core" % "1.2.3",
      "com.github.pureconfig" %% "pureconfig-generic" % "0.13.0",
      "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.13.0",
      "com.lihaoyi" %% "pprint" % "0.6.0"
    )

    httpServer ++ database ++ crypto ++ utils ++ testLibs
  }.map(_.excludeAll(ExclusionRule(organization = "ch.qos.logback"))) // Hvorfor? :P TODO
}
