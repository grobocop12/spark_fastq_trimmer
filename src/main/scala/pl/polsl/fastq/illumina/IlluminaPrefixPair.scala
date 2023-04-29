package pl.polsl.fastq.illumina

import pl.polsl.fastq.data.FastqRecord
import pl.polsl.fastq.illumina.IlluminaPrefixPair.packSeqInternal
import pl.polsl.fastq.trimmer.IlluminaClippingTrimmer.LOG10_4

class IlluminaPrefixPair(basePrefix1: String, basePrefix2: String, val seedMaxMiss: Int, val minPrefix: Int) {
  val (prefix1, prefix2) = createPrefixes(basePrefix1, basePrefix2)

  //  private var prefix1: String = null
  //  private var prefix2: String = null
  private def createPrefixes(prefix1: String, prefix2: String): (String, String) = {
    val length1 = prefix1.length
    val length2 = prefix2.length
    if (length1 != length2) {
      var minLength = length1
      if (length2 < minLength) minLength = length2
      (prefix1.substring(length1 - minLength), prefix2.substring(length2 - minLength))
    } else {
      (prefix1, prefix2)
    }
  }


  private def palindromeReadsCompare(rec1: FastqRecord, rec2: FastqRecord): Integer = {
    val seedMax = seedMaxMiss * 2
    val pack1 = packSeqInternal(prefix1 + rec1.sequence, false)
    val pack2 = packSeqInternal(prefix2 + rec2.sequence, true)
    val prefixLength = prefix1.length
    var testIndex = 0
    var refIndex = prefixLength
    if (pack1.length <= refIndex || pack2.length <= refIndex) return null
    var count = 0
    val seedSkip = prefixLength - 16
    if (seedSkip > 0) {
      testIndex = seedSkip
      count = seedSkip
    }
    var ref1 = 0L
    var ref2 = 0L
    val seqlen1 = rec1.sequence.length + prefixLength
    val seqlen2 = rec2.sequence.length + prefixLength
    val maxCount = (if (seqlen1 > seqlen2) seqlen1
    else seqlen2) - 15 - minPrefix
    while (count < maxCount) {
      ref1 = pack1(refIndex)
      ref2 = pack2(refIndex)

      if ((testIndex < pack2.length && java.lang.Long.bitCount(ref1 ^ pack2(testIndex)) <= seedMax) || (testIndex < pack1.length && java.lang.Long.bitCount(ref2 ^ pack1(testIndex)) <= seedMax)) {
        val totalOverlap = count + prefixLength + 16
        var skip1 = 0
        var skip2 = 0
        if (totalOverlap > seqlen1) skip2 = totalOverlap - seqlen1
        if (totalOverlap > seqlen2) skip1 = totalOverlap - seqlen2
        val actualOverlap = totalOverlap - skip1 - skip2
        val palindromeLikelihood = calculatePalindromeDifferenceQuality(rec1, rec2, actualOverlap, skip1, skip2)
        if (palindromeLikelihood >= minPalindromeLikelihood) return totalOverlap - prefixLength * 2
      }
      count += 1
      val testRefIndex = refIndex + 1
      if (((count & 0x1) == 0) && testRefIndex < pack1.length && testRefIndex < pack2.length) refIndex += 1
      else testIndex += 1
    }
    null
  }

  private def compCh(ch: Char): Char = {
    ch match {
      case 'A' =>
        return 'T'
      case 'C' =>
        return 'G'
      case 'G' =>
        return 'C'
      case 'T' =>
        return 'A'
    }
    'N'
  }

  private def calculatePalindromeDifferenceQuality(rec1: FastqRecord, rec2: FastqRecord, overlap: Int, skip1: Int, skip2: Int): Float = {
    val seq1 = rec1.sequence
    val seq2 = rec2.sequence
    val prefix1 = this.prefix1
    val prefix2 = this.prefix2
    val quals1 = rec1.qualityAsInteger()
    val quals2 = rec2.qualityAsInteger()
    val prefixLength = prefix1.length
    val likelihood = new Array[Float](overlap)
    for (i <- 0 until overlap) {
      val offset1 = i + skip1
      val offset2 = skip2 + overlap - i - 1
      val ch1 = if (offset1 < prefixLength) prefix1.charAt(offset1)
      else seq1.charAt(offset1 - prefixLength)
      var ch2 = if (offset2 < prefixLength) prefix2.charAt(offset2)
      else seq2.charAt(offset2 - prefixLength)
      ch2 = compCh(ch2)
      val qual1 = if (offset1 < prefixLength) 100
      else quals1(offset1 - prefixLength)
      val qual2 = if (offset2 < prefixLength) 100
      else quals2(offset2 - prefixLength)
      if (ch1 == 'N' || ch2 == 'N') likelihood(i) = 0
      else if (ch1 != ch2) if (qual1 < qual2) likelihood(i) = -qual1 / 10
      else likelihood(i) = -qual2 / 10
      else likelihood(i) = LOG10_4
    }
    calculateTotal(likelihood)
  }

  private def calculateTotal(vals: Array[Float]): Float = vals.sum

}

object IlluminaPrefixPair {
  val BASE_A = 0x1
  val BASE_C = 0x4
  val BASE_G = 0x8
  val BASE_T = 0x2

  def apply(prefix1: String, prefix2: String): IlluminaPrefixPair = {
    if (prefix1.length == prefix2.length) {
      new IlluminaPrefixPair(prefix1, prefix2)
    } else {
      val minLength = if (prefix1.length < prefix2.length) prefix1.length else prefix2.length

      new IlluminaPrefixPair(prefix1.substring(prefix1.length - minLength), prefix2.substring(prefix2.length - minLength))
    }
  }

  def packSeqInternal(seq: String, reverse: Boolean): Array[Long] = {
    var out: Array[Long] = null
    if (!reverse) {
      out = new Array[Long](seq.length - 15)
      var pack = 0
      for (i <- 0 until seq.length) {
        val tmp = packCh(seq.charAt(i), false)
        pack = (pack << 4) | tmp
        if (i >= 15) out(i - 15) = pack
      }
    }
    else {
      out = new Array[Long](seq.length - 15)
      var pack = 0
      for (i <- 0 until seq.length) {
        val tmp = packCh(seq.charAt(i), true)
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
      if (offset < seq.length) tmp = packCh(seq.charAt(offset), false)
      pack = (pack << 4) | tmp
      offset += 1
    }
    for (i <- 0 until seq.length) {
      var tmp = 0
      if (offset < seq.length) tmp = packCh(seq.charAt(offset), false)
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