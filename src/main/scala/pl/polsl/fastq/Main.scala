package pl.polsl.fastq

import pl.polsl.fastq.trimmer.{HelpMode, PairedEndMode, SingleEndMode}
import pl.polsl.fastq.utils.{ArgsParser, UsagePrinter}

object Main extends App {
  private def run(args: Array[String]): Unit = {
    val argsMap = ArgsParser.parse(args)
    val mode = argsMap("mode") match {
      case "SE" => new SingleEndMode()
      case "PE" => new PairedEndMode()
      case "help" => new HelpMode()
      case _ => throw new RuntimeException("Unknown mode.")
    }
    mode.run(argsMap)
  }

  try {
    run(args)
  } catch {
    case e: NoSuchElementException => println("Not enough arguments!")
      UsagePrinter.printUsage()
    case e: Exception => e.printStackTrace()
      UsagePrinter.printUsage()
  }
}
