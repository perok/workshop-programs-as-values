import Dependencies._

/* Global / onLoad ~= (_.compose(s => "dependencyUpdates" :: s)) */

addCommandAlias("fix", "all compile:scalafix test:scalafix")
addCommandAlias(
  "fixCheck",
  "; compile:scalafix --check ; test:scalafix --check"
)

// Default settings
inThisBuild(
  List(
    organization := "no.perok",
    scalaVersion := "3.1.0",
    version := "0.1.0-SNAPSHOT",

    // Scalafix
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafixDependencies ++= Seq(
      "com.github.vovapolu" %% "scaluzzi" % "0.1.20",
      "com.github.liancheng" %% "organize-imports" % "0.6.0"
    ),

    // Disable Scaladoc
    Compile / packageDoc / publishArtifact := false,
    packageSrc / publishArtifact := false,
    Compile / doc / sources := Seq.empty,

    // flags
    scalacOptions ++= Seq("-new-syntax", "-indent", "-source", "3.1")
  )
)

val commonSettings = Seq(
  // TODO this does not work for inThisBuild
  libraryDependencies ++= Seq(
    // Standard lib
    "com.disneystreaming" %%% "weaver-cats" % "0.7.7" % Test,
    "org.typelevel" %%% "cats-core" % "2.6.1",
    "org.typelevel" %%% "cats-effect" % "3.2.9",
    "co.fs2" %%% "fs2-core" % fs2Version,
    // Communication
    ("com.softwaremill.sttp.tapir" %%% "tapir-core" % tapirVersion),
    ("com.softwaremill.sttp.tapir" %%% "tapir-json-circe" % tapirVersion),
    // serialization
    "io.circe" %%% "circe-core" % circeVersion,
    "io.circe" %%% "circe-parser" % circeVersion,
    // Utilities
    "dev.optics" %%% "monocle-core" % monocleVersion,
    "eu.timepit" %%% "refined" % "0.9.27"
    /* "eu.timepit" %%% "refined-cats" % "0.9.27" */
  ),
  // Disable fatal warning from sbt-tpolecat plugin when developing
  Test / scalacOptions -= "-Xfatal-warnings",
  scalacOptions --= {
    if (!insideCI.value)
      Seq("-Xfatal-warnings", "-Ywarn-unused:imports")
    else
      Seq.empty
  }
)

// TODO
/* testFrameworks += new TestFramework("weaver.framework.CatsEffect") */

// Ensure that `run` can be stopped with `C-c`
Global / cancelable := true
run / fork := true

publish / skip := true

lazy val root = (project in file("."))
  .disablePlugins(RevolverPlugin)
  .aggregate(backend, frontend)
  .settings(
    name := "Toucan",
    publish := {},
    publishLocal := {}
  )

lazy val shared = (crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure) in file("modules/shared"))
  .settings(
    commonSettings
  )
  .jvmSettings(name := "sharedJVM")
  .jsSettings(name := "sharedJS")

lazy val backend = (project in file("modules/backend"))
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .dependsOn(shared.jvm)
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    commonSettings,
    libraryDependencies ++= backendDependencies,
    Test / fork := true,
    reStart / envVars := Map("APP_ENV" -> "development"),
    reStart / javaOptions ++= Seq(
      "-Dcats.effect.tracing.mode=full"
    ),
    // This settings makes reStart to rebuild if a scala.js file changes on the client
    /* watchSources ++= (frontend / watchSources).value, */
    /* // Setup Docker */
    dockerBaseImage := "eclipse-temurin:8-jre-focal",
    dockerEnvVars := Map("TZ" -> "Europe/Oslo"),
    dockerExposedPorts ++= Seq(8080),
    Docker / packageName := "test",
    dockerRepository := Some("perok")
  )

lazy val frontend = (project in file("modules/frontend"))
  .enablePlugins(ScalaJSPlugin, NpmPlugin)
  .disablePlugins(RevolverPlugin)
  .dependsOn(shared.js)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      // TODO http4s client
      // "com.softwaremill.sttp.tapir" %% "tapir-http4s-client" % tapirVersion
      // "org.http4s" %%% "http4s-dom" % "0.1.0"
      "com.softwaremill.sttp.tapir" %%% "tapir-sttp-client" % tapirVersion,
      "com.softwaremill.sttp.client3" %%% "cats" % "3.3.16",
      "io.github.cquiroz" %%% "scala-java-time" % "2.3.0", // implementations of java.time classes for Scala.JS

      "com.github.japgolly.scalajs-react" %%% "callback" % scalaJsReact,
      "com.github.japgolly.scalajs-react" %%% "callback-ext-cats" % scalaJsReact,
      "com.github.japgolly.scalajs-react" %%% "callback-ext-cats_effect" % scalaJsReact,
      "com.github.japgolly.scalajs-react" %%% "core-bundle-cats_effect" % scalaJsReact,
      "com.github.japgolly.scalajs-react" %%% "extra" % scalaJsReact,
      "com.github.japgolly.scalajs-react" %%% "extra-ext-monocle3" % scalaJsReact,
      "com.github.japgolly.scalajs-react" %%% "test" % scalaJsReact % Test
    ),
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) }
    /*   "auth0-js" -> "9.8.0", */
    /*   "uikit" -> "3.7.4", // todo npm dependencies??? */
    /*   "webpack-merge" -> "5.8.0", */
  )
