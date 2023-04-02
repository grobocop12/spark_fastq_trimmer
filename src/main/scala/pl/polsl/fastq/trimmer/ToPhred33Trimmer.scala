package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord

class ToPhred33Trimmer(phredOffset: Int) extends Trimmer {
  override def apply(in: RDD[FastqRecord]): RDD[FastqRecord] =
    if (phredOffset == 33) in else in.map(convert)


  private def convert(rec: FastqRecord): FastqRecord = {
    FastqRecord(rec.name, rec.sequence, rec.comment, rec.quality.map(f => (f - 31).toChar).mkString)
  }
}
