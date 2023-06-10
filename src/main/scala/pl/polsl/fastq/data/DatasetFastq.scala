package pl.polsl.fastq.data

case class DatasetFastq(name: String, sequence: String, quality: String) {
  if (sequence.length != quality.length) throw new RuntimeException(s"Sequence $sequence does not have the same length as quality string: $quality")

  override def toString = s"$name\n$sequence\n+\n$quality"

  def qualityAsInteger(phredOffset: Int, zeroNs: Boolean = true): Array[Int] = quality.map(c => {
    if (zeroNs && c == 'N') 0 else c.toInt - phredOffset
  }).toArray
}
