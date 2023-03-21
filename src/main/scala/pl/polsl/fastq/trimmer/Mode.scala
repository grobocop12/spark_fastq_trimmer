package pl.polsl.fastq.trimmer

trait Mode {
  def run(argsMap: Map[String, Any]): Unit
}
