package pl.polsl.fastq.trimmer

abstract class Mode(args: Array[String]) {
  def trim(): Unit
}
