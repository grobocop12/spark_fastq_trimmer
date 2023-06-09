package pl.polsl.fastq.trimmer

import pl.polsl.fastq.data.FastqRecord

class ToPhred64Trimmer() extends SingleTrimmer {
  override protected def processRecord(rec: FastqRecord): FastqRecord = {
    if (rec.phredOffset == 64) rec else FastqRecord(rec.name,
      rec.sequence,
      rec.quality.map(f => (f + 31).toChar).mkString,
      64)
  }

}
