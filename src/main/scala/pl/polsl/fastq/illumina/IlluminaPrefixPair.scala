package pl.polsl.fastq.illumina

import pl.polsl.fastq.data.FastqRecord
import pl.polsl.fastq.trimmer.IlluminaClippingTrimmer.{LOG10_4, packSeqInternal}

case class IlluminaPrefixPair(prefix1: String, prefix2: String, seedMaxMiss: Int, minPalindromeLikelihood: Int, minSequenceLikelihood: Int, minPrefix: Int) {

  def palindromeReadsCompare(rec1: FastqRecord, rec2: FastqRecord): Integer = {
    val seedMax = seedMaxMiss * 2
    val pack1 = packSeqInternal(prefix1 + rec1.sequence, reverse = false)
    val pack2 = packSeqInternal(prefix2 + rec2.sequence, reverse = true)
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
    val maxCount = Math.max(seqlen1, seqlen2) - 15 - minPrefix
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
      else if (ch1 != ch2) if (qual1 < qual2) likelihood(i) = -qual1 / 10f
      else likelihood(i) = -qual2 / 10f
      else likelihood(i) = LOG10_4
    }
    calculateTotal(likelihood)
  }

  private def calculateTotal(vals: Array[Float]): Float = vals.sum

}

object IlluminaPrefixPair {
  def apply(prefix1: String, prefix2: String, seedMaxMiss: Int, minPalindromeLikelihood: Int, minSequenceLikelihood: Int, minPrefix: Int): IlluminaPrefixPair = {
    val length1: Int = prefix1.length
    val length2: Int = prefix2.length
    if (length1 != length2) {
      val minLength = Math.min(length1, length2)
      new IlluminaPrefixPair(prefix1.substring(length1 - minLength), prefix2.substring(length2 - minLength), seedMaxMiss, minPalindromeLikelihood, minSequenceLikelihood, minPrefix)
    } else new IlluminaPrefixPair(prefix1, prefix2, seedMaxMiss, minPalindromeLikelihood, minSequenceLikelihood, minPrefix)
  }
}