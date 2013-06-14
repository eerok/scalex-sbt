package ornicar.scalex_sbt

import sbt._, Keys._
import sbt.compiler.AnalyzingCompiler

object ScalexSbtPlugin extends Plugin {

  val defaultSettings = Seq(scalexTask)

  def scalexTaskKey = TaskKey[File]("s", "Generates scalex database.") 

	import java.io.{File, PrintWriter}
  def scalexTask = scalexTaskKey <<= (
    configuration in Compile, 
    dependencyClasspath in Compile,
    streams in Compile,
    compilers in Compile,
    sources in Compile,
    target in Compile,
    scalacOptions in Compile,
    javacOptions in Compile,
    apiMappings in Compile,
    maxErrors in Compile) map {
    (config, depCP, s, cs, srcs, out, sOpts, jOpts, xapis, maxE) =>
    val hasScala = srcs.exists(_.name.endsWith(".scala"))
    val hasJava = srcs.exists(_.name.endsWith(".java"))
    val cp = Attributed.data(depCP).toList
    val label = Defaults.nameForSrc(config.name)
    val (options, runDoc) =
      // if(hasScala)
        (sOpts ++ Opts.doc.externalAPI(xapis), // can't put the .value calls directly here until 2.10.2
          compile(label, new ScalexCompiler(cs.scalac)))
      // else if(hasJava)
      //   (jOpts,
      //     Doc.javadoc(label, s.cacheDirectory / "java", cs.javac.onArgs(exported(s, "javadoc"))))
      // else
        // (Nil, RawCompileLike.nop)
    runDoc(srcs, cp, out, options, maxE, s.log)
    out
  }

  private def compile(label: String, compiler: ScalexCompiler): RawCompileLike.Gen = 
    RawCompileLike.prepare(label + " Scalex database", compiler.index)

  private[this] def exported(w: PrintWriter, command: String): Seq[String] => Unit = args => 
    w.println( (command +: args).mkString(" ") )

	private[this] def exported(s: TaskStreams, command: String): Seq[String] => Unit = args =>
		exported(s.text("export"), command)
}
