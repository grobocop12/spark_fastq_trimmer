package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord

class MaxLenTrimmer(max: Int) extends SingleTrimmer {
  //  override def apply(in: Array[FastqRecord]): Array[FastqRecord] = in.filter(_.sequence.length <= max)

  override protected def processRecord(rec:FastqRecord): FastqRecord =
    if (rec.sequence.length <= max) rec else null
}
