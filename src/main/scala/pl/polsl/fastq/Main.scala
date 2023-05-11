package pl.polsl.fastq

import pl.polsl.fastq.mode.{HelpMode, PairedEndMode, SingleEndMode}
import pl.polsl.fastq.utils.{ArgsParser, UsagePrinter}

import java.util.Date

object Main extends App {
  private def run(args: Array[String]): Unit = {
    val time = new Date().getTime
    val argsMap = ArgsParser.parse(args)
    val mode = argsMap("mode") match {
      case "SE" => new SingleEndMode()
      case "PE" => new PairedEndMode()
      case "help" => new HelpMode()
      case _ => throw new RuntimeException("Unknown mode.")
    }
    mode.run(argsMap)
    println("Milliseconds passed: " + (new Date().getTime - time))
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
