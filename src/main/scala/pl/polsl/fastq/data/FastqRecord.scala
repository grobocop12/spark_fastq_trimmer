package pl.polsl.fastq.data

case class FastqRecord(name: String, sequence: String, comment: String, quality: String, phredOffset: Int = 0) {
  if (sequence.length != quality.length) throw new RuntimeException("Sequence and quality strings must be the same length.")

  override def toString = s"$name\n$sequence\n$comment\n$quality"

  def qualityAsInteger: Array[Int] = quality.map(c => c.toInt - phredOffset).toArray
}