package ornicar.scalex_sbt

import sbt._, Keys._
import sbt.compiler.AnalyzingCompiler

object ScalexSbtPlugin extends Plugin {

  val defaultSettings = Seq(scalexTask)

  def scalexTaskKey = TaskKey[File]("scalex", "Generates scalex database.")

  import java.io.{ File, PrintWriter }
  def scalexTask = scalexTaskKey <<= (
    configuration in Compile,
    dependencyClasspath in Compile,
    streams in Compile,
    compilers in Compile,
    sources in Compile,
    target in Compile,
    scalacOptions in Compile,
    javacOptions in Compile,
    maxErrors in Compile) map {
      (config, depCP, s, cs, srcs, out, sOpts, jOpts, maxE) ⇒
        val hasScala = srcs.exists(_.name.endsWith(".scala"))
        val hasJava = srcs.exists(_.name.endsWith(".java"))
        val cp = depCP.map(_.data).toList
        val label = Defaults.nameForSrc(config.name)
        val scalexCommand = "/home/thib/scalex/scalex index"
        val compiler = new ScalexCompiler(cs.scalac, exported(s, scalexCommand))
        val outputFile = new File(out.getAbsolutePath + "/database.scalex")
        compiler.index(srcs, cp, out, outputFile, sOpts, maxE, s.log)
        outputFile
    }

  // private def compile(label: String, compiler: ScalexCompiler): RawCompileLike.Gen = 
  //   RawCompileLike.prepare(label + " Scalex database", compiler.index)

  private[this] def exported(s: TaskStreams, command: String): Seq[String] ⇒ String = args ⇒ {
    // println( (command +: args).mkString(" ") )
    (command +: args).mkString(" ")
  }
}
