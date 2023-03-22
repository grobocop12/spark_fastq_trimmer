package pl.polsl.fastq.mode

import pl.polsl.fastq.utils.UsagePrinter

class HelpMode extends Mode {
  override def run(argsMap: Map[String, Any]): Unit = {
    UsagePrinter.printUsage()
  }
}
