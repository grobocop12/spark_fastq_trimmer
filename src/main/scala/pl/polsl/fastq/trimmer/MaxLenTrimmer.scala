package pl.polsl.fastq.trimmer

import pl.polsl.fastq.data.FastqRecord

class MaxLenTrimmer(max: Int) extends SingleTrimmer {
  override protected def processRecord(rec: FastqRecord): FastqRecord =
    if (rec.sequence.length <= max) rec else null
}
