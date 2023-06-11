package pl.polsl.fastq.trimmer

import pl.polsl.fastq.data.FastqRecord

class LeadingTrimmer(qual: Int) extends SingleTrimmer {
  override protected def processRecord(rec: FastqRecord): FastqRecord = {
    val idx = rec.qualityAsInteger().indexWhere(_ >= qual)
    if (idx > 0)
      FastqRecord(rec.name,
        rec.sequence.substring(idx),
        rec.quality.substring(idx),
        rec.phredOffset)
    else if (idx == 0)
      rec
    else
      null
  }
}
