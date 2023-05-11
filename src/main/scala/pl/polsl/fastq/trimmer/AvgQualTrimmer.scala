package pl.polsl.fastq.trimmer

import pl.polsl.fastq.data.FastqRecord

class AvgQualTrimmer(quality: Int) extends SingleTrimmer {
  override protected def processRecord(rec:FastqRecord): FastqRecord = {
    val total = rec.qualityAsInteger().sum
    if (total < quality * rec.quality.length) {
      return null
    }
    rec
  }
}
