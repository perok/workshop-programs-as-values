// SBT improvements
/* addSbtPlugin("org.duhemm" % "sbt-errors-summary" % "0.6.3") */
/* addSbtPlugin("ch.epfl.scala" % "sbt-missinglink" % "0.3.2") TODO setup */
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

// Tools
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.3")

// Code improvement
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.20")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.31") // TODO and remove wartremover
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.16")

// Scala.js
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.1.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.7.0")
addSbtPlugin("ch.epfl.scala" % "sbt-web-scalajs-bundler" % "0.20.0")