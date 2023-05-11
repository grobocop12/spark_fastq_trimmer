package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord

class TrailingTrimmer(qual: Int) extends SingleTrimmer {
  //  override def apply(in: Array[FastqRecord]): Array[FastqRecord] =
  //    in.map(this.trimTrailing).filter(_ != null)

  override protected def processRecord(rec:FastqRecord): FastqRecord = {
    val idx = rec.qualityAsInteger().reverse.indexWhere(_ >= qual)
    val length = rec.quality.length
    if (idx > 0 && idx < (length - 1))
      FastqRecord(rec.name,
        rec.sequence.substring(0, length - idx),
        rec.comment,
        rec.quality.substring(0, length - idx),
        rec.phredOffset)
    else if (idx == 0)
      rec
    else
      null
  }

}
