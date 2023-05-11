package pl.polsl.fastq.trimmer

import pl.polsl.fastq.data.FastqRecord

class HeadCropTrimmer(toTrim: Int) extends SingleTrimmer {
  override protected def processRecord(rec:FastqRecord): FastqRecord = {
    if (toTrim >= rec.sequence.length)
      null
    else FastqRecord(rec.name,
      rec.sequence.substring(toTrim),
      rec.comment,
      rec.quality.substring(toTrim),
      rec.phredOffset)
  }
}
