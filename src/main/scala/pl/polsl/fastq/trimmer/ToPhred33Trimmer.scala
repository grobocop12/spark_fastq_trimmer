package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord

class ToPhred33Trimmer() extends SingleTrimmer {
  //  override def apply(in: Array[FastqRecord]): Array[FastqRecord] = in.map(convert)
  override protected def processRecord(rec: FastqRecord): FastqRecord = {
    if (rec.phredOffset == 33)
      rec
    else
      FastqRecord(rec.name,
        rec.sequence,
        rec.comment,
        rec.quality.map(f => (f - 31).toChar).mkString,
        33)
  }
}
