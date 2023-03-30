package pl.polsl.fastq.data

case class FastqRecord(name: String, sequence: String, comment: String, quality: String) {
  if (sequence.length != quality.length) throw new RuntimeException("Sequence and quality strings must be the same length.")

  override def toString = s"$name\n$sequence\n$comment\n$quality"

  lazy val qualityAsInteger: Array[Int] = quality.map(c => c.toInt).toArray
}