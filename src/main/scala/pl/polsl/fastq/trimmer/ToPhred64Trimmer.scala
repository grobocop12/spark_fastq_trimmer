package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord

class ToPhred64Trimmer() extends Trimmer {
  override def apply(in: RDD[FastqRecord]): RDD[FastqRecord] = {
    in.map(convert)
  }

  private def convert(rec: FastqRecord): FastqRecord = {
    if (rec.phredOffset == 64) rec else FastqRecord(rec.name, rec.sequence, rec.comment, rec.quality.map(f => (f + 31).toChar).mkString, 64)
  }
}
