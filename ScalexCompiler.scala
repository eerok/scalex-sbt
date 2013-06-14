package ornicar.scalex_sbt

import java.io.File
import java.net.{ URL, URLClassLoader }
import sbt.compiler.{ AnalyzingCompiler, CompilerArguments, CompileFailed }
import sbt.{ Logger, LoggerReporter }
import sbt.classpath.DualLoader
import xsbti.compile.{ CachedCompiler, CachedCompilerProvider, DependencyChanges, GlobalsCache, CompileProgress, Output }
import xsbti.{ AnalysisCallback, Logger ⇒ xLogger, Reporter }

/**
 * This is a wrapper for
 * compile/src/main/scala/sbt/compiler/AnalyzingCompiler.scala
 * some private functions had to be pasted
 */
private[scalex_sbt] final class ScalexCompiler(compiler: AnalyzingCompiler) {

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
    val arguments = compArgs(sources, classpath, Some(outputDirectory), options)
    // onArgsF(arguments)
    // call("ornicar.scalex_sbt.ScalexInterface", "run", log)(classOf[Array[String]], classOf[xLogger], classOf[Reporter])(
    //   arguments.toArray[String]: Array[String], log, reporter)
    import ornicar.scalex_sbt.ScalexInterface
    (new ScalexInterface).run(arguments.toArray[String], log, reporter)
  }

  private def call(
    interfaceClassName: String,
    methodName: String,
    log: Logger)(argTypes: Class[_]*)(args: AnyRef*): AnyRef =
    {
      val interfaceClass = getInterfaceClass(interfaceClassName, log)
      val interface = interfaceClass.newInstance.asInstanceOf[AnyRef]
      val method = interfaceClass.getMethod(methodName, argTypes: _*)
      try { method.invoke(interface, args: _*) }
      catch {
        case e: java.lang.reflect.InvocationTargetException ⇒
          e.getCause match {
            case c: xsbti.CompileFailed ⇒ throw new CompileFailed(c.arguments, c.toString, c.problems)
            case t                      ⇒ throw t
          }
      }
    }

  private[this] def getInterfaceClass(name: String, log: Logger) = Class.forName(name, true, loader(log))

  protected def createDualLoader(scalaLoader: ClassLoader, sbtLoader: ClassLoader): ClassLoader =
    {
      val xsbtiFilter = (name: String) ⇒ name.startsWith("xsbti.")
      val notXsbtiFilter = (name: String) ⇒ !xsbtiFilter(name)
      new DualLoader(scalaLoader, notXsbtiFilter, x ⇒ true, sbtLoader, xsbtiFilter, x ⇒ false)
    }

  private[this] def loader(log: Logger) =
    {
      val interfaceJar = compiler.provider(compiler.scalaInstance, log)
      // this goes to scalaInstance.loader for scala classes and the loader of this class for xsbti classes
      val dual = createDualLoader(compiler.scalaInstance.loader, getClass.getClassLoader)
      new URLClassLoader(Array(interfaceJar.toURI.toURL), dual)
    }

  override def toString = "Scalex indexing compiler (Scala " + compiler.scalaInstance.actualVersion + ")"
}
