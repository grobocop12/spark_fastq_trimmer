package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord

class TrailingTrimmer(qual: Int) extends Trimmer {
  override def apply(in: RDD[FastqRecord]): RDD[FastqRecord] =
    in.map(this.trimTrailing).filter(_ != null)

  private def trimTrailing(rec: FastqRecord): FastqRecord = {
    val idx = rec.qualityAsInteger().reverse.indexWhere(_ >= qual)
    if (idx > 0)
      FastqRecord(rec.name, rec.sequence.substring(0, idx), rec.comment, rec.quality.substring(0, idx))
    else if (idx == 0)
      rec
    else
      null
  }
}
