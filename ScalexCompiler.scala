package ornicar.scalex_sbt

import java.io.File
import java.net.{ URL, URLClassLoader }
import sbt.compiler.{ AnalyzingCompiler, CompilerArguments, CompileFailed }
import sbt.{ Process, Logger, LoggerReporter }
import sbt.classpath.DualLoader
import xsbti.Reporter

/**
 * This is a wrapper for
 * compile/src/main/scala/sbt/compiler/AnalyzingCompiler.scala
 * some private functions had to be pasted
 */
private[scalex_sbt] final class ScalexCompiler(compiler: AnalyzingCompiler, onArgsF: Seq[String] => String) {

  def index(
    sources: Seq[File],
    classpath: Seq[File],
    outputDirectory: File,
    options: Seq[String],
    maximumErrors: Int,
    log: Logger) {
    index(sources, classpath, outputDirectory, options, log, new LoggerReporter(maximumErrors, log))
  }

  def index(
    sources: Seq[File],
    classpath: Seq[File],
    outputDirectory: File,
    options: Seq[String],
    log: Logger,
    reporter: Reporter) {
    val compArgs = new CompilerArguments(compiler.scalaInstance, compiler.cp)
    val arguments = compArgs(sources, classpath, outputDirectory, options)
    val command = onArgsF(arguments)
    Process(command) ! log
    // val className = "xsbt.ScaladocInterface"
    // val className = "ornicar.scalex_sbt.ScalexInterface"
    // call(className, "run", log)(classOf[Array[String]], classOf[xLogger], classOf[Reporter])(
    //   arguments.toArray[String], log, reporter)
    // import ornicar.scalex_sbt.ScalexInterface
    // (new ScalexInterface).run(arguments.toArray[String], log, reporter)
  }
}
