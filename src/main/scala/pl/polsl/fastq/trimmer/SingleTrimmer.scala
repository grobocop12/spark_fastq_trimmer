package pl.polsl.fastq.trimmer

import pl.polsl.fastq.data.FastqRecord

trait SingleTrimmer extends Trimmer {
  protected def processRecord(rec: FastqRecord): FastqRecord

  override def processSingle(in: FastqRecord): FastqRecord = in match {
    case null => null
    case x: FastqRecord => processRecord(x)
  }

  override def processPair(in: (FastqRecord, FastqRecord)): (FastqRecord, FastqRecord) = (processSingle(in._1), processSingle(in._2))
}
