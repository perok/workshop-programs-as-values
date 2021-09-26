import Dependencies._

// Run update task to verify dependencies are good
// conflictManager := ConflictManager.strict

Global / onChangedBuildSource := ReloadOnSourceChanges

Global / onLoad ~= (_.compose(s => "dependencyUpdates" :: s))

ThisBuild / scalafixDependencies += "com.github.vovapolu" %% "scaluzzi" % "0.1.2"

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

    // flags
    scalacOptions ++= Setup.compilerFlags,
    // TODO remove filterNot Any
    Compile / compile / wartremoverErrors := Warts.unsafe.filterNot(Seq(Wart.Any).contains(_)),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full),
    /* addCompilerPlugin( */
    // semanticdbEnabled := true, // enable Semantic
    // semanticdbVersion := scalafixSemanticdb.revision
    /* ), // TODO version number manual workaround for https://github.com/scalacenter/scalafix/issues/1109 */
    libraryDependencies ++= Seq(
      // Standard lib
      "org.typelevel" %%% "cats-core" % "2.6.1",
      "org.typelevel" %%% "cats-effect" % "3.2.9",
      "co.fs2" %%% "fs2-core" % fs2Version,
      // serialization
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion, // TODO delete
      "io.circe" %%% "circe-generic-extras" % circeVersion, // TODO delete
      "io.circe" %%% "circe-derivation" % "0.13.0-M5",
      "io.circe" %%% "circe-derivation-annotations" % "0.13.0-M5",
      "io.circe" %%% "circe-parser" % circeVersion,
      // TODO bump 3.0
      "com.github.julien-truffaut" %%% "monocle-core" % monocleVersion,
      "com.github.julien-truffaut" %%% "monocle-macro" % monocleVersion
    )
  )
)

val settingsOvveride = Seq(
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
    // TODO TODO TODO hvorfor scala.js stuff her?
    // TODO alt under her er utestet
    scalaJSProjects := Seq(frontend),
    Assets / pipelineStages := Seq(scalaJSPipeline),
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
    watchSources ++= (frontend / watchSources).value,
    // Support stopping the running server
    reStart / mainClass := Some("no.perok.toucan.Main")
  )

lazy val shared = (crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure) in file("modules/shared"))
  .settings(settingsOvveride)
  .jvmSettings(name := "sharedJVM")
  .jsSettings(name := "sharedJS")

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
      // "com.github.japgolly.scalajs-react" %%% "core" % scalaJsReact,
      // "com.github.japgolly.scalajs-react" %%% "ext-monocle-cats" % scalaJsReact,
      // "com.github.japgolly.scalajs-react" %%% "ext-cats" % scalaJsReact,
      // TODO not yet released for scala.js 1.0
      /* "com.olegpy" %%% "shironeko-core" % "0.1.0-RC5", */
      /* "com.olegpy" %%% "shironeko-slinky" % "0.1.0-RC5", */
      "me.shadaj" %%% "slinky-core" % "0.6.8",
      "me.shadaj" %%% "slinky-web" % "0.6.8"
    ),
    Compile / npmDependencies ++= Seq(
      "react" -> "16.8.6",
      "react-dom" -> "16.8.6"
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
      /* "node-sass" -> "4.13.1" */
    ),
    /* Frontend compilation settings */
    Compile / packageJSDependencies / crossTarget := (Compile / resourceManaged).value,
    //webpackBundlingMode := BundlingMode.LibraryOnly(),
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
