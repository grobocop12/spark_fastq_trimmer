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
  val BASE_A = 0x1
  val BASE_C = 0x4
  val BASE_G = 0x8
  val BASE_T = 0x2

  def apply(logger: Logger, args: String): IlluminaClippingTrimmer = {
    val seedMaxMiss = 1
    val minPalindromeLikelihood = 1
    val minSequenceLikelihood = 1
    val minSequenceOverlap = 1
    val minPrefix = 1
    val palindromeKeepBoth = true
    val prefix = "ATCG"
    val seq = "ATCGATCGATCGATCGATCGATCGATCG"
    new IlluminaClippingTrimmer(logger, seedMaxMiss, minPalindromeLikelihood, minSequenceLikelihood, minSequenceOverlap, minPrefix, palindromeKeepBoth,
      List(IlluminaPrefixPair(prefix, prefix, seedMaxMiss, minPalindromeLikelihood, minSequenceLikelihood, minPrefix)),
      Set(new IlluminaShortClippingSeq(seq, calcSingleMask(seq.length), packSeqExternal(seq), seedMaxMiss, minSequenceOverlap, minSequenceLikelihood)),
      Set(new IlluminaMediumClippingSeq(seq, calcSingleMask(seq.length), packSeqExternal(seq), seedMaxMiss, minSequenceOverlap, minSequenceLikelihood)),
      Set(IlluminaLongClippingSeq(seq, calcSingleMask(seq.length), seedMaxMiss, minSequenceOverlap, minSequenceLikelihood)))
  }

  def calcSingleMask(length: Int): Long = {
    var mask = 0xFFFFFFFFFFFFFFFFL
    if (length < 16) mask <<= (16 - length) * 4L
    mask
  }

  def packSeqInternal(seq: String, reverse: Boolean): Array[Long] = {
    var out: Array[Long] = null
    if (!reverse) {
      out = new Array[Long](seq.length - 15)
      var pack = 0
      for (i <- 0 until seq.length) {
        val tmp = packCh(seq.charAt(i), rev = false)
        pack = (pack << 4) | tmp
        if (i >= 15) out(i - 15) = pack
      }
    }
    else {
      out = new Array[Long](seq.length - 15)
      var pack = 0
      for (i <- 0 until seq.length) {
        val tmp = packCh(seq.charAt(i), rev = true)
        pack = (pack >>> 4) | tmp << 60
        if (i >= 15) out(i - 15) = pack
      }
    }
    out
  }

  def packSeqExternal(seq: String): Array[Long] = {
    var out: Array[Long] = null
    out = new Array[Long](seq.length)
    var pack = 0
    var offset = 0
    for (i <- 0 until 15) {
      var tmp = 0
      if (offset < seq.length) tmp = packCh(seq.charAt(offset), rev = false)
      pack = (pack << 4) | tmp
      offset += 1
    }
    for (i <- 0 until seq.length) {
      var tmp = 0
      if (offset < seq.length) tmp = packCh(seq.charAt(offset), rev = false)
      pack = (pack << 4) | tmp
      out(i) = pack
      offset += 1
    }
    out
  }

  private def packCh(ch: Char, rev: Boolean): Int = {
    if (!rev) ch match {
      case 'A' =>
        return BASE_A
      case 'C' =>
        return BASE_C
      case 'G' =>
        return BASE_G
      case 'T' =>
        return BASE_T
    }
    else ch match {
      case 'A' =>
        return BASE_T
      case 'C' =>
        return BASE_G
      case 'G' =>
        return BASE_C
      case 'T' =>
        return BASE_A
    }
    0
  }
}
