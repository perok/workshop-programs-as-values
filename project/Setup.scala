import java.lang.{Runtime => JRuntime}

object Setup {
  val compilerFlags = Seq(
//     "-target:jvm-1.8",
//     // -opt-inline-from 2.12.3
    "-P:acyclic:force",
//     // Scala 2.12.5 flags
    // "Ybackend-parallelism",
    // JRuntime.getRuntime.availableProcessors.toString,
    "-Ycache-plugin-class-loader:last-modified",
    "-Ycache-macro-class-loader:last-modified"
  )
}
