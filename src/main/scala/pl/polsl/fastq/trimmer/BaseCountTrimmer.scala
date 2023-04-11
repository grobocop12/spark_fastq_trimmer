package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord

import java.util

class BaseCountTrimmer(bases: String, minCount: Int, maxCount: Integer) extends Trimmer {
  val set = new util.BitSet()
  bases.foreach(set.set(_))

  override def apply(in: RDD[FastqRecord]): RDD[FastqRecord] = in.map(trim).filter(_ != null)

  private def trim(rec: FastqRecord): FastqRecord = {
    val sum = rec.sequence.count(set.get(_))
    if (sum < minCount) return null
    if (maxCount != null && sum > maxCount) return null
    rec
  }
}
