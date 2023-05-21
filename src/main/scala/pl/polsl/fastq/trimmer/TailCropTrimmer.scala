package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord

class TailCropTrimmer(toTrim: Int) extends SingleTrimmer {
  //  override def apply(in: Array[FastqRecord]): Array[FastqRecord] = in.map(this.headCrop).filter(_ != null)
  override protected def processRecord(rec:FastqRecord): FastqRecord = {
    if (toTrim >= rec.sequence.length)
      null
    else FastqRecord(rec.name,
      rec.sequence.substring(0, rec.sequence.length - toTrim),
      rec.comment,
      rec.quality.substring(0, rec.quality.length - toTrim),
      rec.phredOffset)
  }
}
