package pl.polsl.fastq.data

case class FastqRecord(name: String, sequence: String, quality: String, phredOffset: Int = 0) {
  if (sequence.length != quality.length) throw new RuntimeException(s"Sequence $sequence does not have the same length as quality string: $quality")

  override def toString = s"$name\n$sequence\n+\n$quality"

  def qualityAsInteger(zeroNs: Boolean = true): Array[Int] = quality.map(c => {
    if (zeroNs && c == 'N') 0 else c.toInt - phredOffset
  }).toArray
}