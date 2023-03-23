package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord

class MaxLenTrimmer(val max: Int) extends Trimmer {
  override def processRecord(in: RDD[FastqRecord]): RDD[FastqRecord] = in.filter(_.sequence.length <= max)
}
