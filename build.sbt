import Dependencies._

/* Global / onChangedBuildSource := ReloadOnSourceChanges */

Global / onLoad ~= (_.compose(s => "dependencyUpdates" :: s))

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
  .enablePlugins(WebScalaJSBundlerPlugin) // TODO needed?
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
    // Setup scala.js bundler integration. Access of build artifacts and
    // development compile reloading
    scalaJSProjects := Seq(frontend),
    pipelineStages := Seq(scalaJSPipeline),
    // Allows to read the generated JS on client
    Compile / resources += (frontend / Compile / fastOptJS).value.data,
    // Lets the backend to read the .map file for js
    Compile / resources += (frontend / Compile / fastOptJS).value
      .map((x: sbt.File) => new File(x.getAbsolutePath + ".map"))
      .data,
    // do a fastOptJS on reStart
    reStart := (reStart dependsOn ((frontend / Compile / fastOptJS))).evaluated,
    // TODO must this be done for all assets to be copied? https://scalacenter.github.io/scalajs-bundler/cookbook.html#how-to-get-and-use-a-list-of-assets

    // This settings makes reStart to rebuild if a scala.js file changes on the client
    /* watchSources ++= (frontend / watchSources).value, */
    /* // Setup Docker */
    dockerBaseImage := "eclipse-temurin:8-jre-focal",
    dockerEnvVars := Map("TZ" -> "Europe/Oslo"),
    dockerExposedPorts ++= Seq(8081),
    Docker / packageName := "test",
    dockerRepository := Some("perok")
  )

lazy val frontend = (project in file("modules/frontend"))
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .disablePlugins(RevolverPlugin)
  .dependsOn(shared.js)
  .settings(
    commonSettings,
    /* Frontend dependencies settings */
    libraryDependencies ++= Seq(
      // TODO http4s client
      // "com.softwaremill.sttp.tapir" %% "tapir-http4s-client" % tapirVersion
      // "org.http4s" %%% "http4s-dom" % "0.1.0"
      ("com.softwaremill.sttp.tapir" %%% "tapir-sttp-client" % tapirVersion),
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
    /* WebPack code dependencies settings */
    Compile / npmDependencies ++= Seq(
      "react" -> "17.0.2",
      "react-dom" -> "17.0.2"
    ),
    Compile / npmDevDependencies ++= Seq(
      /* https://github.com/scalacenter/scalajs-bundler/issues/173 */
      /* "scalajs-friendly-source-map-loader" -> "0.1.5", */
      "auth0-js" -> "9.8.0",
      "uikit" -> "3.7.4", // todo npm dependencies???
      "webpack-merge" -> "5.8.0",
      "style-loader" -> "0.23.1",
      "extract-loader" -> "3.1.0",
      "file-loader" -> "2.0.0",
      "css-loader" -> "1.0.1",
      "sass-loader" -> "10.2.0",
      "sass" -> "1.42.1"
    ),
    /* Scala.Js settings */
    scalaJSUseMainModuleInitializer := true, // Is an application, not a library
    /* Frontend compilation settings */
    Compile / packageJSDependencies / crossTarget := (Compile / resourceManaged).value,

    //
    /* WebPack setup settings */
    // TODO see https://github.com/scalacenter/scalajs-bundler/issues/314
    // https://appddeevvmeanderings.blogspot.com/2017
    // Perhaps run the entire thing with create-react-app slightly
    // configured
    webpack / version := "4.46.0",
    webpackConfigFile := Some(baseDirectory.value / "my.custom.webpack.config.js"),
    webpackExtraArgs := Seq("--mode=development"),
    webpackEmitSourceMaps := false,

    // Webpack dev server
    startWebpackDevServer / version := "3.11.2",
    webpackDevServerPort := 8080,
    fastOptJS / webpackDevServerExtraArgs ++= Seq(
      // For index.html i root mappa
      "--content-base",
      (ThisBuild / baseDirectory).value.getAbsolutePath
    ),
    // Reload page on every change
    // TODO --inline reloads the page. But does it hot reload?
    webpackDevServerExtraArgs := Seq("--inline", "--color", "--mode=development")
  )
