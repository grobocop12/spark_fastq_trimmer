package pl.polsl.fastq.trimmer

class SingleEndMode extends Mode {
  override def run(argsMap: Map[String, Any]): Unit = {
    argsMap.foreach(println)
  }
}
