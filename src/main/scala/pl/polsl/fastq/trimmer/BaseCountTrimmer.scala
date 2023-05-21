package pl.polsl.fastq.trimmer

import pl.polsl.fastq.data.FastqRecord

import java.util

class BaseCountTrimmer(bases: String, minCount: Int, maxCount: Integer) extends SingleTrimmer {
  val set = new util.BitSet()
  bases.foreach(set.set(_))

  override protected def processRecord(rec:FastqRecord): FastqRecord = {
    val sum = rec.sequence.count(set.get(_))
    if (sum < minCount) return null
    if (maxCount != null && sum > maxCount) return null
    rec
  }
}
