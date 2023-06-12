package pl.polsl.fastq.trimmer


import pl.polsl.fastq.data.{FastaRecord, FastqRecord}
import pl.polsl.fastq.illumina._
import pl.polsl.fastq.utils.FastaParser

import scala.collection.mutable


class IlluminaClippingTrimmer private(var seedMaxMiss: Int = 0,
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

  override def processSingle(in: FastqRecord): FastqRecord = processRecords((in, null))._1

  override def processPair(in: (FastqRecord, FastqRecord)): (FastqRecord, FastqRecord) = processRecords(in)

  private def processRecords(in: (FastqRecord, FastqRecord)): (FastqRecord, FastqRecord) = {
    var forwardRec: FastqRecord = in._1
    var reverseRec: FastqRecord = in._2
    var toKeepForward: Integer = null
    var toKeepReverse: Integer = null
    if (forwardRec != null && reverseRec != null) {
      // First, check for a palindrome
      for (pair <- prefixPairs) {
        val toKeep = pair.palindromeReadsCompare(forwardRec, reverseRec)
        if (toKeep != null) {
          toKeepForward = min(toKeepForward, toKeep)
          if (palindromeKeepBoth) toKeepReverse = min(toKeepReverse, toKeep)
          else toKeepReverse = 0
        }
      }
    }
    // Also check each record for other nasties
    if (forwardRec != null) {
      if (toKeepForward == null || toKeepForward > 0) {
        for (seq <- forwardSeqs) {
          toKeepForward = min(toKeepForward, seq.readsSeqCompare(forwardRec))
        }
        for (seq <- commonSeqs) {
          toKeepForward = min(toKeepForward, seq.readsSeqCompare(forwardRec))
        }
      }
      // Keep the minimum
      if (toKeepForward != null)
        if (toKeepForward > 0)
          forwardRec = FastqRecord(forwardRec.name,
            forwardRec.sequence.substring(0, toKeepForward),
            forwardRec.quality.substring(0, toKeepForward),
            forwardRec.phredOffset)
        else forwardRec = null
    }
    if (reverseRec != null) {
      if (toKeepReverse == null || toKeepReverse > 0) {
        for (seq <- reverseSeqs) {
          toKeepReverse = min(toKeepReverse, seq.readsSeqCompare(reverseRec))
        }
        for (seq <- commonSeqs) {
          toKeepReverse = min(toKeepReverse, seq.readsSeqCompare(reverseRec))
        }
      }
      // Keep the minimum
      if (toKeepReverse != null) if (toKeepReverse > 0) reverseRec = FastqRecord(reverseRec.name, reverseRec.sequence.substring(0, toKeepReverse), reverseRec.quality.substring(0, toKeepReverse), reverseRec.phredOffset)
      else reverseRec = null
    }
    (forwardRec, reverseRec)
  }

  private def min(a: Integer, b: Integer): Integer = {
    if (a == null) return b
    if (b == null) return a
    if (a < b) a
    else b
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

  def apply(seqs: Array[String], seedMaxMiss: Int, minPalindromeLikelihood: Int, minSequenceLikelihood: Int, minPrefix: Int, palindromeKeepBoth: Boolean): IlluminaClippingTrimmer = {
    val minSequenceOverlap = (minSequenceLikelihood / LOG10_4).toInt
    val (prefixPairs, forward, reverse, common) = loadSequences(seqs, seedMaxMiss, minPalindromeLikelihood, minSequenceLikelihood, minPrefix, minSequenceOverlap)
    new IlluminaClippingTrimmer(seedMaxMiss, minPalindromeLikelihood, minSequenceLikelihood, minSequenceOverlap, minPrefix, palindromeKeepBoth,
      prefixPairs, forward, reverse, common)
  }

  def loadSequences(seqs: Array[String], seedMaxMiss: Int, minPalindromeLikelihood: Int, minSequenceLikelihood: Int, minPrefix: Int, minSequenceOverlap: Int): (List[IlluminaPrefixPair], Set[IlluminaClippingSeq], Set[IlluminaClippingSeq], Set[IlluminaClippingSeq]) = {
    val parser = new FastaParser(seqs)
    parser.parseOne()
    val forwardSeqMap = new mutable.HashMap[String, FastaRecord]
    val reverseSeqMap = new mutable.HashMap[String, FastaRecord]
    val commonSeqMap = new mutable.HashMap[String, FastaRecord]
    val forwardPrefix = new mutable.HashSet[String]
    val reversePrefix = new mutable.HashSet[String]
    while (parser.hasNext) {
      val rec = parser.next
      val name = rec.name
      if (name.endsWith(SUFFIX_F)) {
        forwardSeqMap.put(name, rec)
        if (name.startsWith(PREFIX)) {
          val clippedName = name.substring(0, name.length - SUFFIX_F.length)
          forwardPrefix.add(clippedName)
        }
      }
      else if (name.endsWith(SUFFIX_R)) {
        reverseSeqMap.put(name, rec)
        if (name.startsWith(PREFIX)) {
          val clippedName = name.substring(0, name.length - SUFFIX_R.length)
          reversePrefix.add(clippedName)
        }
      }
      else commonSeqMap.put(name, rec)
    }

    val prefixSet = forwardPrefix.intersect(reversePrefix)
    val prefixPairs = new mutable.ListBuffer[IlluminaPrefixPair]
    for (prefix <- prefixSet) {
      val forwardName = prefix + SUFFIX_F
      val reverseName = prefix + SUFFIX_R
      val forwardRec = forwardSeqMap.remove(forwardName).get
      val reverseRec = reverseSeqMap.remove(reverseName).get
      prefixPairs += new IlluminaPrefixPair(forwardRec.sequence, reverseRec.sequence, seedMaxMiss, minPalindromeLikelihood, minSequenceLikelihood, minPrefix)
    }
    val forwardSeqs = mapClippingSet(forwardSeqMap, seedMaxMiss, minSequenceOverlap, minSequenceLikelihood)
    val reverseSeqs = mapClippingSet(reverseSeqMap, seedMaxMiss, minSequenceOverlap, minSequenceLikelihood)
    val commonSeqs = mapClippingSet(commonSeqMap, seedMaxMiss, minSequenceOverlap, minSequenceLikelihood)
    (prefixPairs.toList, forwardSeqs, reverseSeqs, commonSeqs)
  }

  private def mapClippingSet(map: mutable.Map[String, FastaRecord], seedMaxMiss: Int, minSequenceOverlap: Int, minSequenceLikelihood: Int) = {
    val uniqueSeq = new mutable.HashSet[String]
    val out = new mutable.HashSet[IlluminaClippingSeq]
    for (rec <- map.values) {
      val seq = rec.sequence
      if (!uniqueSeq.contains(seq)) {
        uniqueSeq.add(seq)
        if (seq.length < 16) out.add(new IlluminaShortClippingSeq(seq, calcSingleMask(seq.length), packSeqExternal(seq), seedMaxMiss, minSequenceOverlap, minSequenceLikelihood))
        else if (seq.length < 24) out.add(new IlluminaMediumClippingSeq(seq, calcSingleMask(seq.length), packSeqExternal(seq), seedMaxMiss, minSequenceOverlap, minSequenceLikelihood))
        else out.add(IlluminaLongClippingSeq(seq, calcSingleMask(seq.length), seedMaxMiss, minSequenceOverlap, minSequenceLikelihood))
      }
    }
    out.toSet
  }

  def calcSingleMask(length: Int): Long = {
    var mask = 0xFFFFFFFFFFFFFFFFL
    if (length < 16) mask <<= (16 - length) * 4L
    mask
  }

  def packSeqInternal(seq: String, reverse: Boolean): Array[Long] = {
    if (!reverse) {
      val out = new Array[Long](seq.length - 15)
      var pack = 0L
      for (i <- 0 until seq.length) {
        val tmp = packCh(seq.charAt(i), rev = false)
        pack = (pack << 4) | tmp
        if (i >= 15) out(i - 15) = pack
      }
      out
    }
    else {
      val out = new Array[Long](seq.length - 15)
      var pack = 0L
      for (i <- 0 until seq.length) {
        val tmp = packCh(seq.charAt(i), rev = true)
        pack = (pack >>> 4) | tmp << 60
        if (i >= 15) out(i - 15) = pack
      }
      out
    }
  }

  def packSeqExternal(seq: String): Array[Long] = {
    val out: Array[Long] = new Array[Long](seq.length)
    var pack: Long = 0L
    var offset: Int = 0
    for (_ <- 0 until 15) {
      var tmp: Int = 0
      if (offset < seq.length) {
        tmp = packCh(seq.charAt(offset), rev = false)
      }
      pack = (pack << 4) | tmp
      offset += 1
    }
    for (i <- 0 until seq.length) {
      var tmp: Int = 0
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
        BASE_A
      case 'C' =>
        BASE_C
      case 'G' =>
        BASE_G
      case 'T' =>
        BASE_T
      case _ => 0
    }
    else ch match {
      case 'A' =>
        BASE_T
      case 'C' =>
        BASE_G
      case 'G' =>
        BASE_C
      case 'T' =>
        BASE_A
      case _ => 0
    }
  }
}
