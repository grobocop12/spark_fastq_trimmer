package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord

trait Trimmer {
  def processRecord(in: RDD[FastqRecord]): RDD[FastqRecord]
}
