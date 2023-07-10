package pl.polsl.fastq

import pl.polsl.fastq.mode.{HelpMode, PairedEndMode, PairedEndRowOrientedMode, SingleEndMode}
import pl.polsl.fastq.utils.{ArgsParser, UsagePrinter}

import java.util.Date

object Main extends App {
  private def run(args: Array[String]): Unit = {
    val time = new Date().getTime
    val argsMap = ArgsParser.parse(args)
    val mode = argsMap("mode") match {
      case "SE" => new SingleEndMode()
      case "PE" => new PairedEndMode()
      case "PERO" => new PairedEndRowOrientedMode()
      case "help" => new HelpMode()
      case _ => throw new RuntimeException("Unknown mode.")
    }
    mode.run(argsMap)
    println("Seconds passed: " + ((new Date().getTime - time) / 1000L))
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
