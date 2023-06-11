package pl.polsl.fastq.trimmer

import pl.polsl.fastq.data.FastqRecord

class TailCropTrimmer(toTrim: Int) extends SingleTrimmer {
  override protected def processRecord(rec:FastqRecord): FastqRecord = {
    if (toTrim >= rec.sequence.length)
      null
    else FastqRecord(rec.name,
      rec.sequence.substring(0, rec.sequence.length - toTrim),
      rec.quality.substring(0, rec.quality.length - toTrim),
      rec.phredOffset)
  }
}
