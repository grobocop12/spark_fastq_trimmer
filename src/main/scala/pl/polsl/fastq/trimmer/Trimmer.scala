package pl.polsl.fastq.trimmer

import pl.polsl.fastq.data.FastqRecord

trait Trimmer extends Serializable {
  def processSingle(in: FastqRecord): FastqRecord

  def processPair(in: (FastqRecord, FastqRecord)): (FastqRecord, FastqRecord)
}
