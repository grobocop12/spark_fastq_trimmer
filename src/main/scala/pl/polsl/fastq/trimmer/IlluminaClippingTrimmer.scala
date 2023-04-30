package pl.polsl.fastq.trimmer


import org.apache.log4j.Logger
import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord
import pl.polsl.fastq.illumina._


class IlluminaClippingTrimmer private(logger: Logger,
                                      var seedMaxMiss: Int = 0,
                                      var minPalindromeLikelihood: Int = 0,
                                      var minSequenceLikelihood: Int = 0,
                                      var minSequenceOverlap: Int = 0,
                                      val minPrefix: Int = 0,
                                      val palindromeKeepBoth: Boolean = false,
                                      val prefixPairs: List[IlluminaPrefixPair],
                                      val forwardSeqs: Set[IlluminaClippingSeq],
                                      val reverseSeqs: Set[IlluminaClippingSeq],
                                      val commonSeqs: Set[IlluminaClippingSeq]
                                     ) extends Trimmer {

  override def apply(in: RDD[FastqRecord]): RDD[FastqRecord] = in.map(f => processRecords((f, null))._1).filter(_ != null)

  private def processRecords(records: (FastqRecord, FastqRecord)): (FastqRecord, FastqRecord) = {
    records
  }
}

object IlluminaClippingTrimmer {
  val PREFIX = "Prefix"
  val SUFFIX_F = "/1"
  val SUFFIX_R = "/2"
  val INTERLEAVE = 4
  val LOG10_4 = 0.60206f

  def apply(logger: Logger, args: String): IlluminaClippingTrimmer = {
    val seq = "ATCG"
    new IlluminaClippingTrimmer(logger, 1, 1, 1, 1, 1, true,
      List(IlluminaPrefixPair()),
      Set(new IlluminaShortClippingSeq(seq, IlluminaClippingSeq.calcSingleMask(seq.length), IlluminaClippingSeq.packSeqExternal(seq), 1, 1, 1)),
      Set(new IlluminaMediumClippingSeq(seq)),
      Set(new IlluminaLongClippingSeq(seq)))

  }
}
