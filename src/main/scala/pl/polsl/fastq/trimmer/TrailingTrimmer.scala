package pl.polsl.fastq.trimmer

import pl.polsl.fastq.data.FastqRecord

class TrailingTrimmer(qual: Int) extends SingleTrimmer {
  override protected def processRecord(rec: FastqRecord): FastqRecord = {
    val idx = rec.qualityAsInteger().reverse.indexWhere(_ >= qual)
    val length = rec.quality.length
    if (idx > 0 && idx < (length - 1))
      FastqRecord(rec.name,
        rec.sequence.substring(0, length - idx),
        rec.quality.substring(0, length - idx),
        rec.phredOffset)
    else if (idx == 0 && length > 1)
      rec
    else
      null
  }
}
