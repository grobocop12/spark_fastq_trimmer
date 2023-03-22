package pl.polsl.fastq.mode

trait Mode {
  def run(argsMap: Map[String, Any]): Unit
}
