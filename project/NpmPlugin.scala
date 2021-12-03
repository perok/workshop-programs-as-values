import org.scalajs.sbtplugin.ScalaJSPlugin
import sbt._
import sbt.Keys._
import sbt.io.{IO, Path}
import sbt.nio.file.FileTreeView
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

trait NpmKeys {
  lazy val frontendInstall =
    TaskKey[Seq[File]]("frontendInstall", "Install frontend deps")
  lazy val frontendBuild = TaskKey[Seq[File]]("frontendBuild", "Build the frontend")
}

// TODO yarnplugin
object NpmPlugin extends AutoPlugin {
  override val trigger: PluginTrigger = noTrigger
  override val requires: Plugins = ScalaJSPlugin

  object autoImport extends NpmKeys
  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    frontendInstall := frontendInstallTask.value,
    // TODO fullLinkJS
    // TODO scalaJSLinkerOutputDirectory to webpack
    // TODO linkJS also depends on Compile / resourceGenerators which generates cycles
    // Builds the Scala.js code and runs webpack
    Compile / frontendBuild := frontendBuildTask
      .dependsOn(frontendInstall, Compile / fastLinkJS)
      .value
  )

  //
  // Frontend building
  //
  val frontendDirectory = baseDirectory

  lazy val frontendInstallTask = Def.task {
    import sys.process._
    val s = streams.value
    val logger = s.log

    val _frontendDirectory = frontendDirectory.value

    // TODO should SBT cache this at all?
    val inputFiles = FileTreeView.default
      .list(
        Seq(
          _frontendDirectory.toGlob / "package.json",
          _frontendDirectory.toGlob / "package-lock.json"
        )
      )
      .map(_._1.toFile)

    val cachedFun = FileFunction.cached(
      s.cacheDirectory / "task-install-frontend",
      // Something is touching the files.
      // Therefore we use hash instead
      inStyle = FilesInfo.hash,
      outStyle = FilesInfo.exists
    ) { (_: Set[File]) =>
      val resultCode =
        Process("npm install", _frontendDirectory) ! logger

      if (resultCode != 0) {
        sys.error("Failed to install frontend dependencies")
      }

      FileTreeView.default
        .list(
          Seq(
            _frontendDirectory.toGlob / "node_modules" / ** / *
          )
        )
        .map(_._1.toFile)
        .toSet
    }

    cachedFun(inputFiles.toSet).toSeq
  }

  lazy val frontendBuildTask = Def.task {
    import sys.process._
    val s = streams.value
    val logger = s.log

    val _frontendDirectory = frontendDirectory.value

    val inputFiles = FileTreeView.default
      .list(
        Seq(
          _frontendDirectory.toGlob / "src" / "main" / "css" / ** / *,
          // TODO handled better?
          _frontendDirectory.toGlob / "target" / "scala-3.1.0" / "frontend-fastopt" / ** / *,
          _frontendDirectory.toGlob / "*.json",
          _frontendDirectory.toGlob / "*.js"
        )
      )
      .map(_._1.toFile)

    val buildDirectory = frontendDirectory.value / "dist"
    val outputDirectory = (Compile / resourceManaged).value

    val cachedFun = FileFunction.cached(s.cacheDirectory / "task-build-frontend") {
      (_: Set[File]) =>
        val resultCode = Process("yarn run build", _frontendDirectory) ! logger

        if (resultCode != 0) {
          sys.error("Failed to build frontend")
        }

        IO.copyDirectory(buildDirectory,
                         outputDirectory / "generated" / "frontend",
                         overwrite = true
        )

        Path.allSubpaths(outputDirectory).map(_._1).filterNot(_.isDirectory).toSet
    }

    cachedFun(inputFiles.toSet).toSeq
  }
}
