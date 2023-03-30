package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord

class CropTrimmer(val length: Int) extends Trimmer {
  override def apply(in: RDD[FastqRecord]): RDD[FastqRecord] = in.map(crop)

  private def crop(rec: FastqRecord): FastqRecord = {
    if (rec.sequence.length <= length)
      rec
    else
      FastqRecord(rec.name, rec.sequence.substring(0, length), rec.comment, rec.quality.substring(0, length))
  }
}
