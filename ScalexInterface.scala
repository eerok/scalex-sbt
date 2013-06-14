package ornicar.scalex_sbt

import xsbt.Log, Log.debug
import xsbt.Command
import xsbti.{ Logger, Problem }

/**
 * Based on sbt
 * compile/interface/src/main/scala/xsbt/ScaladocInterface.scala
 */
class ScalexInterface {
  def run(args: Array[String], log: Logger, delegate: xsbti.Reporter) = 
    new Runner(args, log, delegate).run
}

private class Runner(args: Array[String], log: Logger, delegate: xsbti.Reporter) {
  import scala.tools.nsc.{ doc, Global, reporters }
  import reporters.Reporter
  val docSettings: doc.Settings = new doc.Settings(Log.settingsError(log))
  val command = Command(args.toList, docSettings)
  val reporter = DelegatingReporter(docSettings, delegate)
  def noErrors = !reporter.hasErrors && command.ok

  class InterfaceCompileFailed(val arguments: Array[String], val problems: Array[Problem], override val toString: String) extends xsbti.CompileFailed

  import forScope._
  def run() {
    debug(log, "Calling Scaladoc with arguments:\n\t" + args.mkString("\n\t"))
    if (noErrors) {
      import doc._ // 2.8 trunk and Beta1-RC4 have doc.DocFactory.  For other Scala versions, the next line creates forScope.DocFactory
      val processor = new DocFactory(reporter, docSettings)
      processor.document(command.files)
    }
    reporter.printSummary()
    if (!noErrors) throw new InterfaceCompileFailed(args, reporter.problems, "Scaladoc generation failed")
  }

  object forScope {
    class DocFactory(reporter: Reporter, docSettings: doc.Settings) // 2.7 compatibility
    {
      // see https://github.com/paulp/scala-full/commit/649823703a574641407d75d5c073be325ea31307
      trait GlobalCompat {
        def onlyPresentation = false

        def forScaladoc = false
      }

      object compiler extends Global(command.settings, reporter) with GlobalCompat {
        override def onlyPresentation = true
        override def forScaladoc = true
        class DefaultDocDriver // 2.8 source compatibility
        {
          assert(false)
          def process(units: Iterator[CompilationUnit]) = error("for 2.8 compatibility only")
        }
      }
      def document(ignore: Seq[String]) {
        import compiler._
        val run = new Run
        run compile command.files

        val generator =
          {
            import doc._
            new DefaultDocDriver {
              lazy val global: compiler.type = compiler
              lazy val settings = docSettings
            }
          }
        generator.process(run.units)
      }
    }
  }
}
