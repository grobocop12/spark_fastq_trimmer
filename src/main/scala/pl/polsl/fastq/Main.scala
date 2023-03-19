package pl.polsl.fastq

import pl.polsl.fastq.trimmer.{PairedEndMode, SingleEndMode}

object Main extends App {
  private val mode = args.head match {
    case "SE" => new SingleEndMode(args.tail)
    case "PE" => new PairedEndMode(args.tail)
    case _ => throw new RuntimeException("")
  }
  mode.trim()
}
