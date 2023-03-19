package pl.polsl.fastq.trimmer

class SingleEndMode(args: Array[String]) extends Mode(args) {
  override def trim(): Unit = {
    args.foreach(println)
  }
}
