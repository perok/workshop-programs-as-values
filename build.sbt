import Dependencies._

Global / onChangedBuildSource := ReloadOnSourceChanges

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
    scalaVersion := "2.13.6",
    version := "0.1.0-SNAPSHOT",

    // acyclic
    autoCompilerPlugins := true,
    libraryDependencies += "com.lihaoyi" %% "acyclic" % "0.2.1" % "provided",
    addCompilerPlugin("com.lihaoyi" %% "acyclic" % "0.2.1"),

    // Scalafix
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafixDependencies ++= Seq(
      "com.github.vovapolu" %% "scaluzzi" % "0.1.20",
      "com.github.liancheng" %% "organize-imports" % "0.5.0"
    ),

    // Dependency runtime compatability check with sbt-missing-link
    /* missinglinkExcludedDependencies ++= Seq( */
    /*   moduleFilter(organization = "ch.qos.logback", name = "logback-classic"), */
    /*   moduleFilter(organization = "org.slf4j", name = "slf4j-api") */
    /* ), */
    /* missinglinkIgnoreDestinationPackages ++= Seq(IgnoredPackage("org.codehaus")), */
    /* concurrentRestrictions += Tags.limit(missinglinkConflictsTag, 1), */

    // Disable Scaladoc
    Compile / packageDoc / publishArtifact := false,
    packageSrc / publishArtifact := false,
    Compile / doc / sources := Seq.empty,

    // flags
    scalacOptions ++= Setup.compilerFlags,

    // Compiler plugins
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full)
  )
)

val settingsOvveride = Seq(
  // TODO this does not work for inThisBuild
  libraryDependencies ++= Seq(
    // Standard lib
    "org.typelevel" %%% "cats-core" % "2.6.1",
    "org.typelevel" %%% "cats-effect" % "3.2.9",
    "co.fs2" %%% "fs2-core" % fs2Version,
    // Communication
    "com.softwaremill.sttp.tapir" %%% "tapir-core" % tapirVersion,
    "com.softwaremill.sttp.tapir" %%% "tapir-json-circe" % tapirVersion,
    // serialization
    "io.circe" %%% "circe-core" % circeVersion,
    "io.circe" %%% "circe-generic-extras" % circeVersion,
    "io.circe" %%% "circe-derivation" % "0.13.0-M5",
    "io.circe" %%% "circe-derivation-annotations" % "0.13.0-M5",
    "io.circe" %%% "circe-parser" % circeVersion,
    // Utilities
    "dev.optics" %%% "monocle-core" % monocleVersion,
    "dev.optics" %%% "monocle-macro" % monocleVersion,
    "eu.timepit" %% "refined" % "0.9.27",
    "eu.timepit" %% "refined-cats" % "0.9.27"
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

// Ensure that `run` can be stopped with `C-c`
Global / cancelable := true
run / fork := true

publish / skip := true

lazy val root = (project in file("."))
  .disablePlugins(RevolverPlugin)
  .dependsOn(backend, frontend)
  .aggregate(backend, frontend)
  .settings(
    name := "Toucan",
    publish := {},
    publishLocal := {}
  )

lazy val shared = (crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure) in file("modules/shared"))
  .settings(
    settingsOvveride
  )
  .jvmSettings(name := "sharedJVM")
  .jsSettings(name := "sharedJS")

lazy val backend = (project in file("modules/backend"))
  .enablePlugins(WebScalaJSBundlerPlugin) // TODO needed?
  .dependsOn(shared.jvm) //, frontend) dependsOn vs aggregateOn?
  .configs(IntegrationTest)
  .settings(
    //scalacOptions ++= Seq("-Ybackend-parallelism", "4"),
    Defaults.itSettings,
    settingsOvveride,
    libraryDependencies ++= backendDependencies,
    Test / fork := true,
    // Uncomment the following lines to specify a configuration file to load
    Test / javaOptions := Seq("-Dconfig.resource=test.conf"),
    // Support stopping the running server
    reStart / mainClass := Some("no.perok.toucan.Main"),
    // Setup scala.js bundler integration. Access of build artifacts and
    // development compile reloading
    scalaJSProjects := Seq(frontend),
    pipelineStages := Seq(scalaJSPipeline),
    //compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    //pipelineStages := Seq(digest, gzip),
    // TODO burdde disse to være i frontend prosjektet?
//    WebKeys.packagePrefix in Assets := "public/", // TODO er denne nødvendig? - ser ikke ut til det
//    // Automatically add the production-ready assets to classpath
//    (managedClasspath in Runtime) += (packageBin in Assets).value,
//    // TODO Vil bli lagt til i resources pa ferdig bygg?
//    unmanagedResourceDirectories in Assets ++= Seq(
//      (baseDirectory in frontend).value / "src" / "main" / "public"
//    ),
    // Allows to read the generated JS on client
    Compile / resources += (frontend / Compile / fastOptJS).value.data,
    // Lets the backend to read the .map file for js
    Compile / resources += (frontend / Compile / fastOptJS).value
      .map((x: sbt.File) => new File(x.getAbsolutePath + ".map"))
      .data,
    // Lets the server read the jsdeps file
    // Outcommented on 1.0 scalajs TODO
    // (managedResources in Compile) += (artifactPath in (frontend, Compile, packageJSDependencies)).value,
    // do a fastOptJS on reStart
    reStart := (reStart dependsOn ((frontend / Compile / fastOptJS))).evaluated,
    // This settings makes reStart to rebuild if a scala.js file changes on the client
    watchSources ++= (frontend / watchSources).value
    // Setup Docker
    /* dockerBaseImage := "eclipse-temurin:8-jre-focal", */
    /* dockerEnvVars := Map("TZ" -> "Europe/Oslo"), */
    /* dockerExposedPorts ++= Seq(8080), */
    /* Docker / packageName := "applicationcalculation", */
    /* dockerRepository := Some("sgfinans") */
  )

lazy val frontend = (project in file("modules/frontend"))
  .enablePlugins(ScalaJSPlugin,
                 ScalaJSBundlerPlugin
  ) //, ScalaJSWeb, SbtWeb) // TODO SBTweb her? for assets
  .disablePlugins(RevolverPlugin)
  .dependsOn(shared.js)
  .settings(
    settingsOvveride,

    // Outcommented version 1.0 scala.js
    // scalacOptions ++= Seq(
    //   "-P:scalajs:sjsDefinedByDefault"
    // ),
    /* Scala.Js settings */

    scalaJSUseMainModuleInitializer := true, // Is an application, not a library
    /* WebPack setup settings */
    webpackConfigFile := Some(baseDirectory.value / "my.custom.webpack.config.js"),
    webpack / version := "4.43.0",
    startWebpackDevServer / version := "3.11.0",
    /* WebPack code dependencies settings */
    /* Frontend dependencies settings */
    libraryDependencies ++= Seq(
      // TODO https://github.com/http4s/http4s-dom
      "com.softwaremill.sttp.tapir" %%% "tapir-sttp-client" % tapirVersion,
      "io.github.cquiroz" %%% "scala-java-time" % "2.3.0", // implementations of java.time classes for Scala.JS

      "com.github.japgolly.scalajs-react" %%% "callback" % scalaJsReact,
      "com.github.japgolly.scalajs-react" %%% "callback-ext-cats" % scalaJsReact,
      "com.github.japgolly.scalajs-react" %%% "callback-ext-cats_effect" % scalaJsReact,
      "com.github.japgolly.scalajs-react" %%% "core-bundle-cats_effect" % scalaJsReact,
      "com.github.japgolly.scalajs-react" %%% "extra" % scalaJsReact,
      "com.github.japgolly.scalajs-react" %%% "extra-ext-monocle3" % scalaJsReact,
      "com.github.japgolly.scalajs-react" %%% "test" % scalaJsReact % Test
      /* "me.shadaj" %%% "slinky-core" % "0.6.8", */
      /* "me.shadaj" %%% "slinky-web" % "0.6.8" */
    ),
    Compile / npmDependencies ++= Seq(
      "react" -> "17.0.2",
      "react-dom" -> "17.0.2"
    ),
    Compile / npmDevDependencies ++= Seq(
      "auth0-js" -> "9.8.0",
      "uikit" -> "3.5.6", // todo npm dependencies???
      "webpack-merge" -> "4.2.2",
      "style-loader" -> "0.23.1",
      "extract-loader" -> "3.1.0",
      "file-loader" -> "2.0.0",
      "css-loader" -> "1.0.1",
      "sass-loader" -> "10.2.0",
      "sass" -> "1.42.1"
    ),
    /* Frontend compilation settings */
    Compile / packageJSDependencies / crossTarget := (Compile / resourceManaged).value,
    // emitSourceMaps := false, //TODO outcommented because of scalajs-bundler update
    webpackExtraArgs := Seq("--mode=development"),
    // For index.html i root mappa
    fastOptJS / webpackDevServerExtraArgs ++= Seq(
      "--content-base",
      (ThisBuild / baseDirectory).value.getAbsolutePath
    ),
    // Reload page on every change
    webpackDevServerExtraArgs := Seq("--inline", "--color", "--mode=development")
  )
