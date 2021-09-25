import Dependencies._

// Run update task to verify dependencies are good
// conflictManager := ConflictManager.strict

// TODO tpolecat and no fatal warning

Global / onChangedBuildSource := ReloadOnSourceChanges

onLoad in Global ~= (_.compose(s => "dependencyUpdates" :: s))

scalafixDependencies in ThisBuild += "com.github.vovapolu" %% "scaluzzi" % "0.1.2"

addCommandAlias("fix", "all compile:scalafix test:scalafix")
addCommandAlias(
  "fixCheck",
  "; compile:scalafix --check ; test:scalafix --check"
)

lazy val commonSettings = {
  lazy val acyclicSettins = Seq(
    autoCompilerPlugins := true,
    libraryDependencies += "com.lihaoyi" %% "acyclic" % "0.2.1" % "provided",
    addCompilerPlugin("com.lihaoyi" %% "acyclic" % "0.2.1")
  )

  lazy val flags = Seq(
    scalacOptions ++= Setup.compilerFlags,
    scalacOptions -= "-Xfatal-warnings"
    //++ Seq("-Ypartial-unification") // TODO fjern når tpolecat på plass igjen
    ,
    scalacOptions ~= (_.filterNot(
      Set(
        "-Ywarn-unused:imports"
      )
    )),
    scalacOptions in (Compile, console) ~= (_.filterNot(
      Set(
        "-Ywarn-unused:imports",
        "-Xfatal-warnings"
      )
    ))
  )

  // TODO .settings(scalafixSettings, scalafxConfigure(Compile, Test, IntegrationTest))

  acyclicSettins ++ flags ++ Seq(
    // TODO remove filterNot Any
    wartremoverErrors in (Compile, compile) := Warts.unsafe.filterNot(Seq(Wart.Any).contains(_)),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full),
    /* addCompilerPlugin( */
      /* scalafixSemanticdb("4.3.24") */
    /* ), // TODO version number manual workaround for https://github.com/scalacenter/scalafix/issues/1109 */
    libraryDependencies ++= Seq(
      // Standard lib
      "org.typelevel" %%% "cats-core" % "2.6.1",
      "org.typelevel" %%% "cats-effect" % "2.5.4",
      // serialization
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion,
      "io.circe" %%% "circe-generic-extras" % circeVersion,
      "io.circe" %%% "circe-derivation" % "0.13.0-M5",
      "io.circe" %%% "circe-derivation-annotations" % "0.13.0-M5",
      "io.circe" %%% "circe-parser" % circeVersion,
      // State
      "co.fs2" %%% "fs2-core" % fs2Version,
      "com.github.julien-truffaut" %%% "monocle-core" % monocleVersion,
      "com.github.julien-truffaut" %%% "monocle-macro" % monocleVersion
    )
  )
}

lazy val root = (project in file("."))
  .disablePlugins(RevolverPlugin)
  .dependsOn(backend, frontend)
  .aggregate(backend, frontend)
  .settings(
    inThisBuild(
      List(
        organization := "no.perok",
        scalaVersion := "2.13.6",
        version := "0.1.0-SNAPSHOT"
      )
    ),
    name := "Toucan"
  )

lazy val shared = (crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure) in file("modules/shared"))
  .settings(commonSettings)
  .jvmSettings(name := "sharedJVM")
  .jsSettings(name := "sharedJS")

lazy val frontend = (project in file("modules/frontend"))
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin) //, ScalaJSWeb, SbtWeb) // TODO SBTweb her? for assets
  .disablePlugins(RevolverPlugin)
  .dependsOn(shared.js)
  .settings(commonSettings)
  .settings(
    // Outcommented version 1.0 scala.js
    // scalacOptions ++= Seq(
    //   "-P:scalajs:sjsDefinedByDefault"
    // ),
    /* Scala.Js settings */
    scalaJSUseMainModuleInitializer := true,
    /* WebPack setup settings */
    webpackConfigFile := Some(baseDirectory.value / "my.custom.webpack.config.js"),
    version in webpack := "4.43.0",
    version in startWebpackDevServer := "3.11.0",
    /* WebPack code dependencies settings */
    /* Frontend dependencies settings */
    libraryDependencies ++= Seq(
      // "com.github.japgolly.scalajs-react" %%% "core" % scalaJsReact,
      // "com.github.japgolly.scalajs-react" %%% "ext-monocle-cats" % scalaJsReact,
      // "com.github.japgolly.scalajs-react" %%% "ext-cats" % scalaJsReact,
      // TODO not yet released for scala.js 1.0
      /* "com.olegpy" %%% "shironeko-core" % "0.1.0-RC5", */
      /* "com.olegpy" %%% "shironeko-slinky" % "0.1.0-RC5", */
      "me.shadaj" %%% "slinky-core" % "0.6.6",
      "me.shadaj" %%% "slinky-web" % "0.6.6"
    ),
    npmDependencies in Compile ++= Seq(
      "react" -> "16.8.6",
      "react-dom" -> "16.8.6"
    ),
    npmDevDependencies in Compile ++= Seq(
      "auth0-js" -> "9.8.0",
      "uikit" -> "3.5.6", // todo npm dependencies???
      "webpack-merge" -> "4.2.2",
      "style-loader" -> "0.23.1",
      "extract-loader" -> "3.1.0",
      "file-loader" -> "2.0.0",
      "css-loader" -> "1.0.1",
      "sass-loader" -> "10.2.0",
      "sass" -> "1.42.1",
      /* "node-sass" -> "4.13.1" */
    ),
    /* Frontend compilation settings */
    crossTarget in (Compile, packageJSDependencies) := (resourceManaged in Compile).value,
    //webpackBundlingMode := BundlingMode.LibraryOnly(),
    // emitSourceMaps := false, //TODO outcommented because of scalajs-bundler update
    webpackExtraArgs := Seq("--mode=development"),
    // For index.html i root mappa
    webpackDevServerExtraArgs in fastOptJS ++= Seq(
      "--content-base",
      (baseDirectory in ThisBuild).value.getAbsolutePath
    ),
    // Reload page on every change
    webpackDevServerExtraArgs := Seq("--inline", "--color", "--mode=development")
  )

lazy val backend = (project in file("modules/backend"))
  .enablePlugins(WebScalaJSBundlerPlugin)
  .dependsOn(shared.jvm) //, frontend) dependsOn vs aggregateOn?
  .settings(commonSettings)
  .configs(IntegrationTest)
  .settings(
    //scalacOptions ++= Seq("-Ybackend-parallelism", "4"),
    Defaults.itSettings,
    libraryDependencies ++= backendDependencies,
    fork in Test := true,
    // Uncomment the following lines to specify a configuration file to load
    javaOptions in Test := Seq("-Dconfig.resource=test.conf"),
    // TODO alt under her er utestet
    scalaJSProjects := Seq(frontend),
    pipelineStages in Assets := Seq(scalaJSPipeline),
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
    resources in Compile += (fastOptJS in (frontend, Compile)).value.data,
    // Lets the backend to read the .map file for js
    resources in Compile += (fastOptJS in (frontend, Compile)).value
      .map((x: sbt.File) => new File(x.getAbsolutePath + ".map"))
      .data,
    // Lets the server read the jsdeps file
    // Outcommented on 1.0 scalajs TODO
    // (managedResources in Compile) += (artifactPath in (frontend, Compile, packageJSDependencies)).value,
    // do a fastOptJS on reStart
    reStart := (reStart dependsOn (fastOptJS in (frontend, Compile))).evaluated,
    // This settings makes reStart to rebuild if a scala.js file changes on the client
    watchSources ++= (watchSources in frontend).value,
    // Support stopping the running server
    mainClass in reStart := Some("no.perok.toucan.Main")
  )
