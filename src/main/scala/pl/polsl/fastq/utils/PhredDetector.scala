package pl.polsl.fastq.utils

import pl.polsl.fastq.data.FastqRecord

object PhredDetector {
  def apply(fastq: Array[FastqRecord]): Int = {
    val quals = fastq.flatMap(_.qualityAsInteger(false))
    val phred33Total = quals
      .count(q => 33 <= q && q <= 58)
    val phred64Total = quals
      .count(q => 80 <= q && q <= 104)
    if (phred33Total == 0 && phred64Total > 0)
      64
    else if (phred33Total > 0 && phred64Total == 0)
      33
    else throw new RuntimeException("Unable to detect quality encoding")
  }
}
