package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord

class MaxLenTrimmer(max: Int) extends Trimmer {
  override def apply(in: RDD[FastqRecord]): RDD[FastqRecord] = in.filter(_.sequence.length <= max)
}
