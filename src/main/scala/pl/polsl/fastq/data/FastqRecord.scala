package pl.polsl.fastq.data

case class FastqRecord(name: String, sequence: String, comment: String, quality: String, phredOffset: Int = 0) {
  if (sequence.length != quality.length) throw new RuntimeException("Sequence and quality strings must be the same length.")

  override def toString = s"$name\n$sequence\n$comment\n$quality"

  def qualityAsInteger(zeroNs: Boolean = true): Array[Int] = quality.map(c => {
    if (zeroNs && c == 'N') 0 else c.toInt - phredOffset
  }).toArray
}