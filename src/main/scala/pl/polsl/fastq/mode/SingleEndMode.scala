package pl.polsl.fastq.mode

class SingleEndMode extends Mode {
  override def run(argsMap: Map[String, Any]): Unit = {
    argsMap.foreach(println)
  }
}
