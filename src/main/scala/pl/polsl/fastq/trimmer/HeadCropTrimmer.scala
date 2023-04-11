package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord

class HeadCropTrimmer(toTrim: Int) extends Trimmer {
  override def apply(in: RDD[FastqRecord]): RDD[FastqRecord] = in.map(this.headCrop).filter(_ != null)

  private def headCrop(rec: FastqRecord): FastqRecord = {
    if (toTrim >= rec.sequence.length)
      null
    else FastqRecord(rec.name, rec.sequence.substring(toTrim), rec.comment, rec.quality.substring(toTrim), rec.phredOffset)
  }
}
