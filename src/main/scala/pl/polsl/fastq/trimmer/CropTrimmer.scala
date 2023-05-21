package pl.polsl.fastq.trimmer

import pl.polsl.fastq.data.FastqRecord

class CropTrimmer(length: Int) extends SingleTrimmer {
  override protected def processRecord(rec:FastqRecord): FastqRecord = {
    if (rec.sequence.length <= length)
      rec
    else
      FastqRecord(rec.name,
        rec.sequence.substring(0, length),
        rec.comment,
        rec.quality.substring(0, length),
        rec.phredOffset)
  }
}
