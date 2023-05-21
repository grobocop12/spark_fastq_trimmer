package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord

class MinLenTrimmer(min: Int) extends SingleTrimmer {
  //  override def apply(in: Array[FastqRecord]): Array[FastqRecord] = in.filter(_.sequence.length >= min)

  override protected def processRecord(rec:FastqRecord): FastqRecord =
    if (rec.sequence.length >= min) rec else null
}
