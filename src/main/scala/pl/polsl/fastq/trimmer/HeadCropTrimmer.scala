package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord

class HeadCropTrimmer(length: Int) extends Trimmer {
  override def apply(in: RDD[FastqRecord]): RDD[FastqRecord] = in.map(this.headCrop).filter(_ != null)

  private def headCrop(rec: FastqRecord): FastqRecord = {
    if (length >= rec.sequence.length)
      null
    else FastqRecord(rec.name, rec.sequence.substring(length), rec.comment, rec.quality.substring(length), rec.phredOffset)
  }
}
