package pl.polsl.fastq.trimmer

import pl.polsl.fastq.data.FastqRecord

class MinLenTrimmer(min: Int) extends SingleTrimmer {
  override protected def processRecord(rec:FastqRecord): FastqRecord =
    if (rec.sequence.length >= min) rec else null
}
