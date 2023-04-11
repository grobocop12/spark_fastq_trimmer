package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord

class AvgQualTrimmer(quality: Int) extends Trimmer {
  override def apply(in: RDD[FastqRecord]): RDD[FastqRecord] = in.map(trim).filter(_ != null)

  private def trim(rec: FastqRecord): FastqRecord = {
    val total = rec.qualityAsInteger().sum
    if (total < quality * rec.quality.length) {
      return null
    }
    rec
  }
}
