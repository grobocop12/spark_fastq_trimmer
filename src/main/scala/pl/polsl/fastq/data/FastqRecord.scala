package pl.polsl.fastq.data

case class FastqRecord(name: String, sequence: String, comment: String, quality: String) {
  override def toString = s"$name\n$sequence\n$comment\n$quality"
}